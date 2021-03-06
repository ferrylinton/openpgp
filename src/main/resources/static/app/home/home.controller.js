(function() {

	'use strict';

	angular.module('openpgp').controller('homeController', homeController);

	homeController.$inject = [ '$scope', '$state', '$http' ];

	function homeController($scope, $state, $http ) {

		var vm = this;
		
		vm.encrypt = { 
				data : 'test 1234',
				passwords : ['password'],
				armor : false,
				result : ''
		};
		
		vm.decrypt = { 
				message : '',
				format : 'utf8',
				password : vm.encrypt.passwords[0],
				result : ''
		};
		
		vm.submitEncrypt = submitEncrypt;
		vm.submitDecrypt = submitDecrypt;
		
		
		function submitEncrypt(){
			
			vm.encrypt.data = vm.encrypt.data;
			
			openpgp.encrypt(vm.encrypt).then(function(ciphertext) {
				
				var encrypted = ciphertext.message.packets.write();
				vm.encrypt.result = s2r(encrypted, '');
				vm.decrypt.message = encrypted;
				
				$scope.$apply();
			});
		}
		
		function submitDecrypt(){
			var options = JSON.parse(JSON.stringify(vm.decrypt));
			options.message = openpgp.message.read(r2s(vm.decrypt.message));
			
			console.log(isUint8Array(options.message));
			openpgp.decrypt(options).then(function(plaintext) {
				vm.decrypt.result = isUint8Array(plaintext.data) ? new TextDecoder("utf-8").decode(plaintext.data) : plaintext.data;
				$scope.$apply();
			});
		}
		
		function string_to_bytes ( str, utf8 ) {
		    utf8 = !!utf8;

		    var len = str.length,
		        bytes = new Uint8Array( utf8 ? 4*len : len );

		    for ( var i = 0, j = 0; i < len; i++ ) {
		        var c = str.charCodeAt(i);

		        if ( utf8 && 0xd800 <= c && c <= 0xdbff ) {
		            if ( ++i >= len ) throw new Error( "Malformed string, low surrogate expected at position " + i );
		            c = ( (c ^ 0xd800) << 10 ) | 0x10000 | ( str.charCodeAt(i) ^ 0xdc00 );
		        }
		        else if ( !utf8 && c >>> 8 ) {
		            throw new Error("Wide characters are not allowed.");
		        }

		        if ( !utf8 || c <= 0x7f ) {
		            bytes[j++] = c;
		        }
		        else if ( c <= 0x7ff ) {
		            bytes[j++] = 0xc0 | (c >> 6);
		            bytes[j++] = 0x80 | (c & 0x3f);
		        }
		        else if ( c <= 0xffff ) {
		            bytes[j++] = 0xe0 | (c >> 12);
		            bytes[j++] = 0x80 | (c >> 6 & 0x3f);
		            bytes[j++] = 0x80 | (c & 0x3f);
		        }
		        else {
		            bytes[j++] = 0xf0 | (c >> 18);
		            bytes[j++] = 0x80 | (c >> 12 & 0x3f);
		            bytes[j++] = 0x80 | (c >> 6 & 0x3f);
		            bytes[j++] = 0x80 | (c & 0x3f);
		        }
		    }

		    return bytes.subarray(0, j);
		}
		
		function isUint8Array(data) {
		    return Uint8Array.prototype.isPrototypeOf(data);
		  }
		
		var b64s = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

		/**

		 * Convert binary array to radix-64

		 * @param {Uint8Array} t Uint8Array to convert

		 * @returns {string} radix-64 version of input string

		 * @static

		 */

		function s2r(t, o) {

		  // TODO check btoa alternative

		  var a, c, n;

		  var r = o ? o : [],

		      l = 0,

		      s = 0;

		  var tl = t.length;

		  for (n = 0; n < tl; n++) {

		    c = t[n];

		    if (s === 0) {

		      r.push(b64s.charAt((c >> 2) & 63));

		      a = (c & 3) << 4;

		    } else if (s === 1) {

		      r.push(b64s.charAt((a | (c >> 4) & 15)));

		      a = (c & 15) << 2;

		    } else if (s === 2) {

		      r.push(b64s.charAt(a | ((c >> 6) & 3)));

		      l += 1;

		      if ((l % 60) === 0) {

		        r.push("\n");

		      }

		      r.push(b64s.charAt(c & 63));

		    }

		    l += 1;

		    if ((l % 60) === 0) {

		      r.push("\n");

		    }

		    s += 1;

		    if (s === 3) {

		      s = 0;

		    }

		  }

		  if (s > 0) {

		    r.push(b64s.charAt(a));

		    l += 1;

		    if ((l % 60) === 0) {

		      r.push("\n");

		    }

		    r.push('=');

		    l += 1;

		  }

		  if (s === 1) {

		    if ((l % 60) === 0) {

		      r.push("\n");

		    }

		    r.push('=');

		  }

		  if (o)

		  {

		    return;

		  }

		  return r.join('');

		}

		/**

		 * Convert radix-64 to binary array

		 * @param {String} t radix-64 string to convert

		 * @returns {Uint8Array} binary array version of input string

		 * @static

		 */

		function r2s(t) {

		  // TODO check atob alternative

		  var c, n;

		  var r = [],

		    s = 0,

		    a = 0;

		  var tl = t.length;

		  for (n = 0; n < tl; n++) {

		    c = b64s.indexOf(t.charAt(n));

		    if (c >= 0) {

		      if (s) {

		        r.push(a | (c >> (6 - s)) & 255);

		      }

		      s = (s + 2) & 7;

		      a = (c << s) & 255;

		    }

		  }

		  return new Uint8Array(r);

		}


	}

})();
