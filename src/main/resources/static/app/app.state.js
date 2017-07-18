(function() {
	'use strict';

	angular.module('openpgp').config(stateConfig);

	stateConfig.$inject = [ '$stateProvider', '$urlRouterProvider' ];

	function stateConfig($stateProvider, $urlRouterProvider) {
		
		$urlRouterProvider.when('', 'home');

		$urlRouterProvider.otherwise('/home');
		
	}
})();
