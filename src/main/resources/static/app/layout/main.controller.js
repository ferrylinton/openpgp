(function() {

	'use strict';

	angular.module('openpgp').controller('mainController', mainController);

	mainController.$inject = [ '$scope', '$state', '$stateParams', '$http' ];

	function mainController($scope, $state, $stateParams, $http ) {

		var vm = this;
		vm.init = init;
		vm.goTo = goTo;

		function init(){
			vm.active = $stateParams.index;
			
			if($stateParams.index == null){
				$state.go('home', {index:0});
			}
		}
		
		function goTo(url, idx){
			$state.go(url, {index:idx});			
		}
		
	}

})();
