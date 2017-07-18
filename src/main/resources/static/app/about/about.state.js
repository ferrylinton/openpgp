(function() {
	
	'use strict';

	angular.module('openpgp').config(stateConfig);

	stateConfig.$inject = [ '$stateProvider' ];

	function stateConfig($stateProvider) {
		$stateProvider.state('about', {
			url : '/about',
			params: {
	            index: null
	        },
			views : {
				'@' : {
					templateUrl : 'app/layout/main.html',
					controller : 'mainController',
					controllerAs: 'vm'
				},
				'content@about' : {
					templateUrl : 'app/about/about.html',
					controller : 'aboutController',
					controllerAs: 'vm'
				}
			}
		});
	}
})();
