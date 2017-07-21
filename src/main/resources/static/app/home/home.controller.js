(function() {

	'use strict';

	angular.module('openpgp').controller('homeController', homeController);

	homeController.$inject = [ '$scope', '$state', '$http' ];

	function homeController($scope, $state, $http ) {

		var vm = this;
		
		vm.encrypt = { 
				data : 'test 1234',
				passwords : ['password'],
				armor : true,
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
			
			if(!vm.encrypt.armor){
				
				vm.encrypt.data = string_to_bytes(vm.encrypt.data, 'utf8');
				console.log(vm.encrypt.data);
				console.log(isUint8Array(vm.encrypt.data));
				console.log(new TextDecoder("utf-8").decode(vm.encrypt.data));
			}
			
			openpgp.encrypt(vm.encrypt).then(function(ciphertext) {
				
				if(vm.encrypt.armor){
					vm.encrypt.result = ciphertext.data;
					vm.decrypt.message = ciphertext.data;
					vm.decrypt.format = 'utf8';
				}else{

					var encrypted = ciphertext.message.packets.write();
					vm.encrypt.result = encrypted;
					vm.decrypt.message = encrypted;
					console.log(isUint8Array(encrypted));
					vm.decrypt.format = 'binary';
				}
				
				$scope.$apply();
			});
		}
		
		function submitDecrypt(){
			var options = JSON.parse(JSON.stringify(vm.decrypt));
			console.log(isUint8Array(vm.decrypt.message));
			if(vm.encrypt.armor){
				options.message = openpgp.message.readArmored(options.message);
			}else{
				options.message = openpgp.message.read(vm.decrypt.message);
			}
			
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
	}

})();
