angular.module('ui.router')
.controller('historyController', ['$scope', '$http', '$stateParams', '$state', function($scope, $http, $stateParams, $state, $mdDialog) {
    const SRV = 'http://localhost:8080';

    var today = new Date()
    var dd = today.getDay()
    if (dd == 6) {
        dd = 0
    }
    else {
        dd++;
    }
    let $constructor = function() {
        $http.get(SRV + '/locations/1')
        .then(function(response) {
            $scope.locations = response.data
            console.log($scope.locations.length);
        })
        .catch(function(error) {
            console.log(error)
        })

        $http.get(SRV + '/histories/1')
        .then(function(response) {
            $scope.histories = response.data
            console.log($scope.histories.length);
        })
        .catch(function(error) {
            console.log(error)
        })

        $http.get(SRV + '/locations/' + parseInt(dd) + '/1')
        .then(function(response) {
            $scope.locationsDay = response.data
            console.log($scope.locationsDay.length);
        })
        .catch(function(error) {
            console.log(error)
        })

        var millisecondsToWait = 1000;
        setTimeout(function() {
            var sc = document.createElement("link");
            sc.setAttribute("rel", "import");
            sc.setAttribute("href", "https://user-content-dot-custom-elements.appspot.com/GoogleWebComponents/google-map/1.2.0/google-map/google-map.html");
            document.head.appendChild(sc);
        }, millisecondsToWait);
    }

    var uniqLocations = [];
    var frequencyLocations;
    var procent;

    $scope.test = function() {
        procent = 0;
        frequencyLocations = new Map();
        var max = 0;
        uniqLocations = [];

        for (var i = 0; i < $scope.locationsDay.length - 1; i++) {
            var location1 = {
                id: $scope.locationsDay[i].id,
                lat: $scope.locationsDay[i].lat,
                lgn: $scope.locationsDay[i].lgn
            };
            for (var j = i + 1; j < $scope.locationsDay.length; j++) {
                var location2 = {
                    id: $scope.locationsDay[j].id,
                    lat: $scope.locationsDay[j].lat,
                    lgn: $scope.locationsDay[j].lgn
                };
                if (areInTheSamePlace(location1, location2) && !arrayContainsLocation(location1)) {
                    uniqLocations.push(location1);
                }
            }
        }
        var millisecondsToWait = 200;
        setTimeout(function() {
            console.log("uniq: " + uniqLocations.length);

            for (var loc1 in uniqLocations) {
                max = 0;
                for (var loc2 in $scope.locationsDay) {
                    if (areInTheSamePlace(uniqLocations[loc1], $scope.locationsDay[loc2])) {
                        max++;
                    }
                        //console.log(uniqLocations[loc1]);
                    }
                    frequencyLocations.set(uniqLocations[loc1], max);
                    procent += max;
                    // console.log("loc freq " + max + " - " + uniqLocations[loc1].lat + ", " + uniqLocations[loc1].lgn);
                }

                var millisecondsToWait2 = 500;
                setTimeout(function() {
                    getLocationWithFrequenciMax();
                }, millisecondsToWait2);
            }, millisecondsToWait);
    }

    function arrayContainsLocation(location) {
        for (var i = 0; i < uniqLocations.length; i++) {
            if (distanceBetween2Locations(location, uniqLocations[i]) < 0.2)
                return true;
        }
        return false;
    }

    function areInTheSamePlace(loc1, loc2) {
        if (distanceBetween2Locations(loc1, loc2) < 0.2) return true;
        return false;

    }

    function distanceBetween2Locations(loc1, loc2) {
            var R = 6371; // Radius of the earth in km
            var dLat = deg2rad(loc2.lat - loc1.lat); // deg2rad below
            var dLon = deg2rad(loc2.lgn - loc1.lgn);
            var a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(deg2rad(loc1.lat)) * Math.cos(deg2rad(loc2.lat)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            var d = R * c; // Distance in km#
            //console.log("distance: " + d);

            return d;
        }

        function deg2rad(deg) {
            return deg * (Math.PI / 180)
        }
        var max = 0;
        var restaurat;
        function getLocationWithFrequenciMax() {
            max = 0;
            var locTmp;

            frequencyLocations.forEach(function(value, key) {
                if (value > max) {
                    max = value;
                    locTmp = key;
                }
            });
            console.log(locTmp.id + ": " + locTmp.lat + ", " + locTmp.lgn);

            console.log(getUrl(locTmp.lat, locTmp.lgn, 'restaurant'));

            loadJSON(function(response) {
                var actual_JSON = JSON.parse(response);
                console.log(actual_JSON);
                console.log(actual_JSON.results.length);

                var nearbyPlaceRestaurant = actual_JSON.results;
                restaurat = nearbyPlaceRestaurant[0];

                var location = {
                    id: 0,
                    lat: restaurat.geometry.location.lat,
                    lgn: restaurat.geometry.location.lng
                };

                var distance = distanceBetween2Locations(locTmp, location);
                console.log("distanta dintre locatie si restaurant: " + distance);
                for (var i in nearbyPlaceRestaurant) {
                    location.lat = nearbyPlaceRestaurant[i].geometry.location.lat;
                    location.lgn = nearbyPlaceRestaurant[i].geometry.location.lng;
                    var dis = distanceBetween2Locations(locTmp, location);
                    if (dis < distance) {
                        distance = dis;
                        restaurat = nearbyPlaceRestaurant[i];
                    }
                    console.log("distanta: " + dis);
                }
                console.log(restaurat);
                console.log(distance);

                codeAddress({lat: parseFloat(locTmp.lat), lng: parseFloat(locTmp.lgn)});
                
            }, locTmp.lat, locTmp.lgn);
        }

        function codeAddress(latLng) {
            var geocoder = new google.maps.Geocoder();
            geocoder.geocode({
                'location' : latLng
            }, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    var address = results[0].formatted_address;
                    var ceva = "Cu o probabilitate de " + (max / procent * 100).toFixed(2) + "%, maine vei fi aici: " + address +
                    ". \n Nu ai vrea sa mananci la noi, " + restaurat.name + "? Avem un raiting bun: " + restaurat.rating +
                    ". \n Ne gasesti la " + restaurat.vicinity; 

                    $scope.detalii = ceva;
                    alert(ceva);
                } else {
                    alert("Geocode was not successful for the following reason: " + status);
                }
            });
        }

        function loadJSON(callback, loc1, loc2) {
            var xobj = new XMLHttpRequest();
            // xobj.overrideMimeType("application/json");;

            xobj.open('GET', getUrl(loc1, loc2, 'restaurant'), 'jsonp', true);

            xobj.setRequestHeader('Content-Type', 'application/json, charset=UTF-8');
            xobj.setRequestHeader('Access-Control-Allow-Origin', "*/*");
            xobj.setRequestHeader('Access-Control-Allow-Headers', 'access-control-allow-headers,access-control-allow-methods,access-control-allow-origin,content-type');
            xobj.setRequestHeader('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS')

            xobj.onreadystatechange = function() {
                if (xobj.readyState == 4 && xobj.status == "200") {
                    callback(xobj.responseText);
                }
            };
            xobj.send();
        }

        function getUrl(latitude, longitude, nearbyPlace) {
            var googlePlacesUrl = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?';
            googlePlacesUrl += 'location=' + latitude + "," + longitude;
            googlePlacesUrl += '&radius=' + 3000;
            googlePlacesUrl += "&type=" + nearbyPlace;
            googlePlacesUrl += "&sensor=true";
            googlePlacesUrl += "&key=" + "AIzaSyBtcLOoMu0UYiLH8NqgpjKXq9zzz1dV5FU";
            return googlePlacesUrl;
        }

        function minutesLocation(location) {
            var minutes = 0;
            var hourStart = parseInt(location.oraInceput.split(":")[0]);
            var minutesStart = parseInt(location.oraInceput.split(":")[1]);
            var hourFinish = parseInt(location.oraSfarsit.split(":")[0]);
            var minutesFinish = parseInt(location.oraSfarsit.split(":")[1]);
            console.log("detalii", hourStart + ":" + minutesStart + " ---- " + hourFinish + ":" + minutesFinish);
            if (minutesStart < minutesFinish) {
                minutes += minutesFinish - minutesStart;
            }
            else if (minutesStart > minutesFinish) {
                minutes += 60 - minutesStart + minutesFinish;
            }
            return minutes + (hourFinish - hourStart) * 60;
        }

        $constructor()
    }])
