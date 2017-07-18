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
				password : vm.encrypt.passwords[0],
				result : ''
		};
		
		vm.submitEncrypt = submitEncrypt;
		vm.submitDecrypt = submitDecrypt;
		
		
		function submitEncrypt(){
			openpgp.encrypt(vm.encrypt).then(function(ciphertext) {
				vm.encrypt.result = ciphertext.data;
				vm.decrypt.message = ciphertext.data;
				$scope.$apply();
			});
		}
		
		function submitDecrypt(){
			var options = JSON.parse(JSON.stringify(vm.decrypt));
			options.message = openpgp.message.readArmored(options.message);
			
			openpgp.decrypt(options).then(function(plaintext) {
				vm.decrypt.result = plaintext.data;
				$scope.$apply();
			});
		}
		
	}

})();
