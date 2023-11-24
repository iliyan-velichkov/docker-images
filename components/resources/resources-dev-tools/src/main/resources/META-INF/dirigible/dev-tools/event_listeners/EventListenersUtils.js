// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import * as SDK from '../sdk/sdk.js';
import * as Common from '../common/common.js';

/**
 * @param {!SDK.RemoteObject.RemoteObject} object
 * @return {!Promise<!FrameworkEventListenersObject>}
 */
export function frameworkEventListeners(object) {
  const domDebuggerModel = object.runtimeModel().target().model(SDK.DOMDebuggerModel.DOMDebuggerModel);
  if (!domDebuggerModel) {
    // TODO(kozyatinskiy): figure out how this should work for |window|.
    return Promise.resolve(
        /** @type {!FrameworkEventListenersObject} */ ({eventListeners: [], internalHandlers: null}));
  }

  const listenersResult = /** @type {!FrameworkEventListenersObject} */ ({eventListeners: []});
  return object.callFunction(frameworkEventListenersImpl, undefined)
      .then(assertCallFunctionResult)
      .then(getOwnProperties)
      .then(createEventListeners)
      .then(returnResult)
      .catch(error => {
        console.error(error);
        return listenersResult;
      });

  /**
   * @param {!SDK.RemoteObject.RemoteObject} object
   * @return {!Promise<!SDK.RemoteObject.GetPropertiesResult>}
   */
  function getOwnProperties(object) {
    return object.getOwnProperties(false /* generatePreview */);
  }

  /**
   * @param {!SDK.RemoteObject.GetPropertiesResult} result
   * @return {!Promise<undefined>}
   */
  function createEventListeners(result) {
    if (!result.properties) {
      throw new Error('Object properties is empty');
    }
    const promises = [];
    for (const property of result.properties) {
      if (property.name === 'eventListeners' && property.value) {
        promises.push(convertToEventListeners(property.value).then(storeEventListeners));
      }
      if (property.name === 'internalHandlers' && property.value) {
        promises.push(convertToInternalHandlers(property.value).then(storeInternalHandlers));
      }
      if (property.name === 'errorString' && property.value) {
        printErrorString(property.value);
      }
    }
    return /** @type {!Promise<undefined>} */ (Promise.all(promises));
  }

  /**
   * @param {!SDK.RemoteObject.RemoteObject} pageEventListenersObject
   * @return {!Promise<!Array<!SDK.DOMDebuggerModel.EventListener>>}
   */
  function convertToEventListeners(pageEventListenersObject) {
    return SDK.RemoteObject.RemoteArray.objectAsArray(pageEventListenersObject)
        .map(toEventListener)
        .then(filterOutEmptyObjects);

    /**
     * @param {!SDK.RemoteObject.RemoteObject} listenerObject
     * @return {!Promise<?SDK.DOMDebuggerModel.EventListener>}
     */
    function toEventListener(listenerObject) {
      /** @type {string} */
      let type;
      /** @type {boolean} */
      let useCapture;
      /** @type {boolean} */
      let passive;
      /** @type {boolean} */
      let once;
      /** @type {?SDK.RemoteObject.RemoteObject} */
      let handler = null;
      /** @type {?SDK.RemoteObject.RemoteObject} */
      let originalHandler = null;
      /** @type {?SDK.DebuggerModel.Location} */
      let location = null;
      /** @type {?SDK.RemoteObject.RemoteObject} */
      let removeFunctionObject = null;

      const promises = [];
      promises.push(listenerObject.callFunctionJSON(truncatePageEventListener, undefined).then(storeTruncatedListener));

      /**
       * @suppressReceiverCheck
       * @this {EventListenerObjectInInspectedPage}
       * @return {!{type:string, useCapture:boolean, passive:boolean, once:boolean}}
       */
      function truncatePageEventListener() {
        return {type: this.type, useCapture: this.useCapture, passive: this.passive, once: this.once};
      }

      /**
       * @param {!{type:string, useCapture: boolean, passive: boolean, once: boolean}} truncatedListener
       */
      function storeTruncatedListener(truncatedListener) {
        type = truncatedListener.type;
        useCapture = truncatedListener.useCapture;
        passive = truncatedListener.passive;
        once = truncatedListener.once;
      }

      promises.push(listenerObject.callFunction(handlerFunction)
                        .then(assertCallFunctionResult)
                        .then(storeOriginalHandler)
                        .then(toTargetFunction)
                        .then(storeFunctionWithDetails));

      /**
       * @suppressReceiverCheck
       * @return {function()}
       * @this {EventListenerObjectInInspectedPage}
       */
      function handlerFunction() {
        return this.handler;
      }

      /**
       * @param {!SDK.RemoteObject.RemoteObject} functionObject
       * @return {!SDK.RemoteObject.RemoteObject}
       */
      function storeOriginalHandler(functionObject) {
        originalHandler = functionObject;
        return originalHandler;
      }

      /**
       * @param {!SDK.RemoteObject.RemoteObject} functionObject
       * @return {!Promise<undefined>}
       */
      function storeFunctionWithDetails(functionObject) {
        handler = functionObject;
        return /** @type {!Promise<undefined>} */ (
            functionObject.debuggerModel().functionDetailsPromise(functionObject).then(storeFunctionDetails));
      }

      /**
       * @param {?SDK.DebuggerModel.FunctionDetails} functionDetails
       */
      function storeFunctionDetails(functionDetails) {
        location = functionDetails ? functionDetails.location : null;
      }

      promises.push(
          listenerObject.callFunction(getRemoveFunction).then(assertCallFunctionResult).then(storeRemoveFunction));

      /**
       * @suppressReceiverCheck
       * @return {function()}
       * @this {EventListenerObjectInInspectedPage}
       */
      function getRemoveFunction() {
        return this.remove;
      }

      /**
       * @param {!SDK.RemoteObject.RemoteObject} functionObject
       */
      function storeRemoveFunction(functionObject) {
        if (functionObject.type !== 'function') {
          return;
        }
        removeFunctionObject = functionObject;
      }

      return Promise.all(promises).then(createEventListener).catch(error => {
        console.error(error);
        return null;
      });

      /**
       * @return {!SDK.DOMDebuggerModel.EventListener}
       */
      function createEventListener() {
        if (!location) {
          throw new Error('Empty event listener\'s location');
        }
        return new SDK.DOMDebuggerModel.EventListener(
            /** @type {!SDK.DOMDebuggerModel.DOMDebuggerModel} */ (domDebuggerModel), object, type, useCapture, passive,
            once, handler, originalHandler, location, removeFunctionObject,
            SDK.DOMDebuggerModel.EventListener.Origin.FrameworkUser);
      }
    }
  }

  /**
   * @param {!SDK.RemoteObject.RemoteObject} pageInternalHandlersObject
   * @return {!Promise<!SDK.RemoteObject.RemoteArray>}
   */
  function convertToInternalHandlers(pageInternalHandlersObject) {
    return SDK.RemoteObject.RemoteArray.objectAsArray(pageInternalHandlersObject)
        .map(toTargetFunction)
        .then(SDK.RemoteObject.RemoteArray.createFromRemoteObjects.bind(null));
  }

  /**
   * @param {!SDK.RemoteObject.RemoteObject} functionObject
   * @return {!Promise<!SDK.RemoteObject.RemoteObject>}
   */
  function toTargetFunction(functionObject) {
    return SDK.RemoteObject.RemoteFunction.objectAsFunction(functionObject).targetFunction();
  }

  /**
   * @param {!Array<!SDK.DOMDebuggerModel.EventListener>} eventListeners
   */
  function storeEventListeners(eventListeners) {
    listenersResult.eventListeners = eventListeners;
  }

  /**
   * @param {!SDK.RemoteObject.RemoteArray} internalHandlers
   */
  function storeInternalHandlers(internalHandlers) {
    listenersResult.internalHandlers = internalHandlers;
  }

  /**
   * @param {!SDK.RemoteObject.RemoteObject} errorString
   */
  function printErrorString(errorString) {
    Common.Console.Console.instance().error(String(errorString.value));
  }

  /**
   * @return {!FrameworkEventListenersObject}
   */
  function returnResult() {
    return listenersResult;
  }

  /**
   * @param {!SDK.RemoteObject.CallFunctionResult} result
   * @return {!SDK.RemoteObject.RemoteObject}
   */
  function assertCallFunctionResult(result) {
    if (result.wasThrown || !result.object) {
      throw new Error('Exception in callFunction or empty result');
    }
    return result.object;
  }

  /**
   * @param {!Array<?T>} objects
   * @return {!Array<!T>}
   * @template T
   */
  function filterOutEmptyObjects(objects) {
    return objects.filter(filterOutEmpty);

    /**
     * @param {?T} object
     * @return {boolean}
     * @template T
     */
    function filterOutEmpty(object) {
      return !!object;
    }
  }

  /*
    frameworkEventListeners fetcher functions should produce following output:
        {
          // framework event listeners
          "eventListeners": [
            {
              "handler": function(),
              "useCapture": true,
              "passive": false,
              "once": false,
              "type": "change",
              "remove": function(type, handler, useCapture, passive)
            },
            ...
          ],
          // internal framework event handlers
          "internalHandlers": [
            function(),
            function(),
            ...
          ]
        }
    */
  /**
   * @suppressReceiverCheck
   * @return {!{eventListeners:!Array<!EventListenerObjectInInspectedPage>, internalHandlers:?Array<function()>}}
   * @this {Object}
   */
  function frameworkEventListenersImpl() {
    const errorLines = [];
    let eventListeners = [];
    let internalHandlers = [];
    let fetchers = [jQueryFetcher];
    try {
      if (self.devtoolsFrameworkEventListeners && isArrayLike(self.devtoolsFrameworkEventListeners)) {
        fetchers = fetchers.concat(self.devtoolsFrameworkEventListeners);
      }
    } catch (e) {
      errorLines.push('devtoolsFrameworkEventListeners call produced error: ' + toString(e));
    }

    for (let i = 0; i < fetchers.length; ++i) {
      try {
        const fetcherResult = fetchers[i](this);
        if (fetcherResult.eventListeners && isArrayLike(fetcherResult.eventListeners)) {
          eventListeners =
              eventListeners.concat(fetcherResult.eventListeners.map(checkEventListener).filter(nonEmptyObject));
        }
        if (fetcherResult.internalHandlers && isArrayLike(fetcherResult.internalHandlers)) {
          internalHandlers =
              internalHandlers.concat(fetcherResult.internalHandlers.map(checkInternalHandler).filter(nonEmptyObject));
        }
      } catch (e) {
        errorLines.push('fetcher call produced error: ' + toString(e));
      }
    }
    const result = {eventListeners: eventListeners};
    if (internalHandlers.length) {
      result.internalHandlers = internalHandlers;
    }
    if (errorLines.length) {
      let errorString = 'Framework Event Listeners API Errors:\n\t' + errorLines.join('\n\t');
      errorString = errorString.substr(0, errorString.length - 1);
      result.errorString = errorString;
    }
    return result;

    /**
     * @param {?Object} obj
     * @return {boolean}
     */
    function isArrayLike(obj) {
      if (!obj || typeof obj !== 'object') {
        return false;
      }
      try {
        if (typeof obj.splice === 'function') {
          const len = obj.length;
          return typeof len === 'number' && (len >>> 0 === len && (len > 0 || 1 / len > 0));
        }
      } catch (e) {
      }
      return false;
    }

    /**
     * @param {*} eventListener
     * @return {?EventListenerObjectInInspectedPage}
     */
    function checkEventListener(eventListener) {
      try {
        let errorString = '';
        if (!eventListener) {
          errorString += 'empty event listener, ';
        }
        const type = eventListener.type;
        if (!type || (typeof type !== 'string')) {
          errorString += 'event listener\'s type isn\'t string or empty, ';
        }
        const useCapture = eventListener.useCapture;
        if (typeof useCapture !== 'boolean') {
          errorString += 'event listener\'s useCapture isn\'t boolean or undefined, ';
        }
        const passive = eventListener.passive;
        if (typeof passive !== 'boolean') {
          errorString += 'event listener\'s passive isn\'t boolean or undefined, ';
        }
        const once = eventListener.once;
        if (typeof once !== 'boolean') {
          errorString += 'event listener\'s once isn\'t boolean or undefined, ';
        }
        const handler = eventListener.handler;
        if (!handler || (typeof handler !== 'function')) {
          errorString += 'event listener\'s handler isn\'t a function or empty, ';
        }
        const remove = eventListener.remove;
        if (remove && (typeof remove !== 'function')) {
          errorString += 'event listener\'s remove isn\'t a function, ';
        }
        if (!errorString) {
          return {type: type, useCapture: useCapture, passive: passive, once: once, handler: handler, remove: remove};
        }
        errorLines.push(errorString.substr(0, errorString.length - 2));
        return null;
      } catch (error) {
        errorLines.push(toString(error));
        return null;
      }
    }

    /**
     * @param {*} handler
     * @return {function()|null}
     */
    function checkInternalHandler(handler) {
      if (handler && (typeof handler === 'function')) {
        return handler;
      }
      errorLines.push('internal handler isn\'t a function or empty');
      return null;
    }

    /**
     * @param {*} obj
     * @return {string}
     * @suppress {uselessCode}
     */
    function toString(obj) {
      try {
        return '' + obj;
      } catch (e) {
        return '<error>';
      }
    }

    /**
     * @param {*} obj
     * @return {boolean}
     */
    function nonEmptyObject(obj) {
      return !!obj;
    }

    function jQueryFetcher(node) {
      if (!node || !(node instanceof Node)) {
        return {eventListeners: []};
      }
      const jQuery = /** @type {?{fn,data,_data}}*/ (window['jQuery']);
      if (!jQuery || !jQuery.fn) {
        return {eventListeners: []};
      }
      const jQueryFunction = /** @type {function(!Node)} */ (jQuery);
      const data = jQuery._data || jQuery.data;

      const eventListeners = [];
      const internalHandlers = [];

      if (typeof data === 'function') {
        const events = data(node, 'events');
        for (const type in events) {
          for (const key in events[type]) {
            const frameworkListener = events[type][key];
            if (typeof frameworkListener === 'object' || typeof frameworkListener === 'function') {
              const listener = {
                handler: frameworkListener.handler || frameworkListener,
                useCapture: true,
                passive: false,
                once: false,
                type: type
              };
              listener.remove = jQueryRemove.bind(node, frameworkListener.selector);
              eventListeners.push(listener);
            }
          }
        }
        const nodeData = data(node);
        if (nodeData && typeof nodeData.handle === 'function') {
          internalHandlers.push(nodeData.handle);
        }
      }
      const entry = jQueryFunction(node)[0];
      if (entry) {
        const entryEvents = entry['$events'];
        for (const type in entryEvents) {
          const events = entryEvents[type];
          for (const key in events) {
            if (typeof events[key] === 'function') {
              const listener = {handler: events[key], useCapture: true, passive: false, once: false, type: type};
              // We don't support removing for old version < 1.4 of jQuery because it doesn't provide API for getting "selector".
              eventListeners.push(listener);
            }
          }
        }
        if (entry && entry['$handle']) {
          internalHandlers.push(entry['$handle']);
        }
      }
      return {eventListeners: eventListeners, internalHandlers: internalHandlers};
    }

    /**
     * @param {string} selector
     * @param {string} type
     * @param {function()} handler
     * @this {?Object}
     */
    function jQueryRemove(selector, type, handler) {
      if (!this || !(this instanceof Node)) {
        return;
      }
      const node = /** @type {!Node} */ (this);
      const jQuery = /** @type {?{fn,data,_data}}*/ (window['jQuery']);
      if (!jQuery || !jQuery.fn) {
        return;
      }
      const jQueryFunction = /** @type {function(!Node)} */ (jQuery);
      jQueryFunction(node).off(type, selector, handler);
    }
  }
}

/** @typedef {{eventListeners:!Array<!SDK.DOMDebuggerModel.EventListener>, internalHandlers:?SDK.RemoteObject.RemoteArray}} */
export let FrameworkEventListenersObject;

/** @typedef {{type: string, useCapture: boolean, passive: boolean, once: boolean, handler: function()}} */
export let EventListenerObjectInInspectedPage;
