/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Class: mxSelectionCellsHandler
 * 
 * An event handler that manages cell handlers and invokes their mouse event
 * processing functions.
 * 
 * Group: Events
 * 
 * Event: mxEvent.ADD
 * 
 * Fires if a cell has been added to the selection. The <code>state</code>
 * property contains the <mxCellState> that has been added.
 * 
 * Event: mxEvent.REMOVE
 * 
 * Fires if a cell has been remove from the selection. The <code>state</code>
 * property contains the <mxCellState> that has been removed.
 * 
 * Parameters:
 * 
 * graph - Reference to the enclosing <mxGraph>.
 */
function mxSelectionCellsHandler(graph)
{
	mxEventSource.call(this);
	
	this.graph = graph;
	this.handlers = new mxDictionary();
	this.graph.addMouseListener(this);
	
	this.refreshHandler = mxUtils.bind(this, function(sender, evt)
	{
		if (this.isEnabled())
		{
			this.refresh();
		}
	});
	
	this.graph.getSelectionModel().addListener(mxEvent.CHANGE, this.refreshHandler);
	this.graph.getModel().addListener(mxEvent.CHANGE, this.refreshHandler);
	this.graph.getView().addListener(mxEvent.SCALE, this.refreshHandler);
	this.graph.getView().addListener(mxEvent.TRANSLATE, this.refreshHandler);
	this.graph.getView().addListener(mxEvent.SCALE_AND_TRANSLATE, this.refreshHandler);
	this.graph.getView().addListener(mxEvent.DOWN, this.refreshHandler);
	this.graph.getView().addListener(mxEvent.UP, this.refreshHandler);
};

/**
 * Extends mxEventSource.
 */
mxUtils.extend(mxSelectionCellsHandler, mxEventSource);

/**
 * Variable: graph
 * 
 * Reference to the enclosing <mxGraph>.
 */
mxSelectionCellsHandler.prototype.graph = null;

/**
 * Variable: enabled
 * 
 * Specifies if events are handled. Default is true.
 */
mxSelectionCellsHandler.prototype.enabled = true;

/**
 * Variable: refreshHandler
 * 
 * Keeps a reference to an event listener for later removal.
 */
mxSelectionCellsHandler.prototype.refreshHandler = null;

/**
 * Variable: maxHandlers
 * 
 * Defines the maximum number of handlers to paint individually. Default is 100.
 */
mxSelectionCellsHandler.prototype.maxHandlers = 100;

/**
 * Variable: handlers
 * 
 * <mxDictionary> that maps from cells to handlers.
 */
mxSelectionCellsHandler.prototype.handlers = null;

/**
 * Function: isEnabled
 * 
 * Returns <enabled>.
 */
mxSelectionCellsHandler.prototype.isEnabled = function()
{
	return this.enabled;
};

/**
 * Function: setEnabled
 * 
 * Sets <enabled>.
 */
mxSelectionCellsHandler.prototype.setEnabled = function(value)
{
	this.enabled = value;
};

/**
 * Function: getHandler
 * 
 * Returns the handler for the given cell.
 */
mxSelectionCellsHandler.prototype.getHandler = function(cell)
{
	return this.handlers.get(cell);
};

/**
 * Function: reset
 * 
 * Resets all handlers.
 */
mxSelectionCellsHandler.prototype.reset = function()
{
	this.handlers.visit(function(key, handler)
	{
		handler.reset.apply(handler);
	});
};

/**
 * Function: refresh
 * 
 * Reloads or updates all handlers.
 */
mxSelectionCellsHandler.prototype.refresh = function()
{
	// Removes all existing handlers
	var oldHandlers = this.handlers;
	this.handlers = new mxDictionary();
	
	// Creates handles for all selection cells
	var tmp = this.graph.getSelectionCells();

	for (var i = 0; i < tmp.length; i++)
	{
		var state = this.graph.view.getState(tmp[i]);

		if (state != null)
		{
			var handler = oldHandlers.remove(tmp[i]);

			if (handler != null)
			{
				if (handler.state != state)
				{
					handler.destroy();
					handler = null;
				}
				else if (!this.isHandlerActive(handler))
				{
					if (handler.refresh != null)
					{
						handler.refresh();
					}
					
					handler.redraw();
				}
			}
			
			if (handler == null)
			{
				handler = this.graph.createHandler(state);
				this.fireEvent(new mxEventObject(mxEvent.ADD, 'state', state));
			}
			
			if (handler != null)
			{
				this.handlers.put(tmp[i], handler);
			}
		}
	}
	
	// Destroys all unused handlers
	oldHandlers.visit(mxUtils.bind(this, function(key, handler)
	{
		this.fireEvent(new mxEventObject(mxEvent.REMOVE, 'state', handler.state));
		handler.destroy();
	}));
};

/**
 * Function: isHandlerActive
 * 
 * Returns true if the given handler is active and should not be redrawn.
 */
mxSelectionCellsHandler.prototype.isHandlerActive = function(handler)
{
	return handler.index != null;
};

/**
 * Function: updateHandler
 * 
 * Updates the handler for the given shape if one exists.
 */
mxSelectionCellsHandler.prototype.updateHandler = function(state)
{
	var handler = this.handlers.remove(state.cell);
	
	if (handler != null)
	{
		handler.destroy();
		handler = this.graph.createHandler(state);
		
		if (handler != null)
		{
			this.handlers.put(state.cell, handler);
		}
	}
};

/**
 * Function: mouseDown
 * 
 * Redirects the given event to the handlers.
 */
mxSelectionCellsHandler.prototype.mouseDown = function(sender, me)
{
	if (this.graph.isEnabled() && this.isEnabled())
	{
		var args = [sender, me];

		this.handlers.visit(function(key, handler)
		{
			handler.mouseDown.apply(handler, args);
		});
	}
};

/**
 * Function: mouseMove
 * 
 * Redirects the given event to the handlers.
 */
mxSelectionCellsHandler.prototype.mouseMove = function(sender, me)
{
	if (this.graph.isEnabled() && this.isEnabled())
	{
		var args = [sender, me];

		this.handlers.visit(function(key, handler)
		{
			handler.mouseMove.apply(handler, args);
		});
	}
};

/**
 * Function: mouseUp
 * 
 * Redirects the given event to the handlers.
 */
mxSelectionCellsHandler.prototype.mouseUp = function(sender, me)
{
	if (this.graph.isEnabled() && this.isEnabled())
	{
		var args = [sender, me];

		this.handlers.visit(function(key, handler)
		{
			handler.mouseUp.apply(handler, args);
		});
	}
};

/**
 * Function: destroy
 * 
 * Destroys the handler and all its resources and DOM nodes.
 */
mxSelectionCellsHandler.prototype.destroy = function()
{
	this.graph.removeMouseListener(this);
	
	if (this.refreshHandler != null)
	{
		this.graph.getSelectionModel().removeListener(this.refreshHandler);
		this.graph.getModel().removeListener(this.refreshHandler);
		this.graph.getView().removeListener(this.refreshHandler);
		this.refreshHandler = null;
	}
};
