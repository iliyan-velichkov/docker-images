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
var qrCodeGenerator = require('utils/qrcode');
var assertTrue      = require('test/assert').assertTrue;

var input           = "Dirigible";
var expectedResult  = [-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 1, 44, 0, 0, 1, 44, 1, 0, 0,
      0, 0, 81, 6, -77, -40, 0, 0, 1, 42, 73, 68, 65, 84, 120, 94, -19, -42, 75, 14, -62, 48, 12, 4, -48, -80, -30, 24, 61, 106,
      115, -44, 30, -127, 101, 87, 24, 102, -20, 126, 100, 33, 1, 18, 56, -101, -103, 69, -107, 56, -81, -85, 81, 10, -51, 62, -55,
       -83, -27, -55, -53, -120, -91, -120, -91, -120, -91, -120, -91, -120, -91, -120, -91, 124, -57, -42, 22, -103, -42, 11, -74,
        23, -21, 49, -64, 86, -84, -120, -7, -50, 58, 88, -97, -72, 58, 31, -120, 21, -79, -114, 98, -114, -61, -25, 11, 123, 119,
         98, 67, -104, -79, 54, -79, 97, 12, -121, -57, 74, -84, -104, 49, 60, -100, 23, -78, -13, -127, 88, 9, 99, 69, -47, 78,
          -25, 35, 6, 98, 117, -20, -56, -118, 57, 31, -25, -120, -91, -23, 95, 24, -54, -102, -20, -66, -107, -123, 21, 123, -62,
           -27, 17, -85, 98, -58, 111, 22, 70, 96, -19, -70, -65, 16, -75, -119, 85, -78, -83, 39, 116, 39, 54, -128, -15, -34, -52,
            -122, 67, -108, 53, -5, 54, 34, 86, -62, 112, 71, 108, 107, 12, 93, -51, -2, -51, 74, 101, -119, -3, -107, -19, 1, -61,
             -67, 97, 109, 123, -60, 42, -40, -118, 126, 16, -120, -83, -74, -72, 65, 98, 101, 44, -118, -31, -107, -15, -19, -126,
              -81, -41, 114, -11, -71, 88, 17, -21, -8, 7, -37, -3, -74, -96, 49, -2, -122, -120, -115, 99, -24, -87, -95, -74, -25,
               86, 108, 4, -13, 85, 124, -77, 56, 19, 43, 100, 76, -25, -113, 6, -53, 114, 27, 17, 43, 97, 62, -10, -117, 98, -117,
                -81, -64, -30, 125, -79, 10, -10, 46, 98, 41, 98, 41, 98, 41, 98, 41, 98, 41, 98, 41, 63, 102, 15, 78, -18, -101, -38,
                 49, 35, 78, 66, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126];

var result = qrCodeGenerator.generateQRCode(input);

function arraysEqual(a, b) {
  if (a === b) return true;
  if (a == null || b == null) return false;
  if (a.length !== b.length) return false;

  for (var i = 0; i < a.length; ++i) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

var isSame = arraysEqual(result, expectedResult);

console.log('isSameMe: ' + isSame);
// TODO - doublecheck results
//assertTrue(isSame);