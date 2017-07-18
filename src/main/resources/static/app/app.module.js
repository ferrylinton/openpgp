(function() {
	'use strict';

	angular
		.module('openpgp', [ 
			'ui.router', 
			'ui.bootstrap', 
			'ngCookies', 
			'angularMoment', 
			'pascalprecht.translate', 
			'ngSanitize',
			'ngToast'
			])
		.run(run);

	run.$inject = [ '$rootScope', '$transitions', '$trace', '$state', '$stateParams', '$cookies', '$translate'];

	function run($rootScope, $transitions, $trace, $state, $stateParams, $cookies, $translate) {
		$rootScope.transitions = [];

		if($cookies.get(LANGUAGE) === undefined){
			$cookies.put(LANGUAGE, ENGLISH);
		};
		
		$translate.use($cookies.get(LANGUAGE));

	}

})();
