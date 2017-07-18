(function() {
	
	'use strict';

	angular.module('openpgp').config(stateConfig);

	stateConfig.$inject = [ '$stateProvider' ];

	function stateConfig($stateProvider) {
		$stateProvider.state('home', {
			url : '/home',
			params: {
	            index: null
	        },
			views : {
				'@' : {
					templateUrl : 'app/layout/main.html',
					controller : 'mainController',
					controllerAs: 'vm'
				},
				'content@home' : {
					templateUrl : 'app/home/home.html',
					controller : 'homeController',
					controllerAs: 'vm'
				}
			}
		});
	}
})();
