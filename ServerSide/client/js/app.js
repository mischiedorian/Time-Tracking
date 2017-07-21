const app = angular.module('cafenea',['ui.router'])

app.config(['$stateProvider', '$urlRouterProvider',function($stateProvider, $urlRouterProvider){
    $urlRouterProvider.otherwise('/histories')
    $stateProvider
        .state('history', {
            url : '/histories',
            templateUrl : 'views/history.html',
            controller: 'historyController'
        })
}])