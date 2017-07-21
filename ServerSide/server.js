var express = require('express');

const bodyParser = require('body-parser');
const Sequelize = require('sequelize');

const Client = require('node-rest-client').Client;

const sequelize = new Sequelize('dorian', 'dorian', '123456', {
  dialect: 'mysql',
  port: 3306
});

const SRV = 'http://localhost:8080';

var app = express();
var client = new Client();
app.use(bodyParser.json());
app.use(express.static(__dirname + '/client'));

var mysql = require('mysql');

var con = mysql.createConnection({
  host: "localhost",
  user: "dorian",
  password: "123456",
  database: "dorian",
  port: "3306"
});

con.connect(function(err) {
  if (err) {
    console.log('Eroare conectare la baza!');
    return;
  }
  console.log('Conexiune reusita la baza de date.');
});

var Location = sequelize.define('location', {
  zi: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {

    }
  },
  luna: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {

    }
  },
  ziDinLuna: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {

    }
  },
  oraInceput: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  oraSfarsit: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  lat: {
    allowNull: false,
    type: Sequelize.DOUBLE,
    validate: {

    }
  },
  lgn: {
    allowNull: false,
    type: Sequelize.DOUBLE,
    validate: {

    }
  },
  idUser: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {

    }
  }
})

var History = sequelize.define('history', {
  loc: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  produs: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  ora: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  data: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  idUser: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {

    }
  }
})

var User = sequelize.define('user', {
  email: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  name: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {

    }
  },
  imgPic: {
    allowNull: true,
    type: Sequelize.STRING,
    validate: {

    }
  },
  token: {
    allowNull: true,
    type: Sequelize.STRING,
    validate: {

    }
  }
})

var Product = sequelize.define('product', {
  name: {
    allowNull: false,
    type: Sequelize.STRING,
    validate: {}
  },
  quantity: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {}
  },
  idUser: {
    allowNull: false,
    type: Sequelize.INTEGER,
    validate: {}
  },
  idLocation: {
    allowNull: true,
    type: Sequelize.INTEGER,
    validate: {}
  }
})

app.get('/createdb', function(req, res) {
  sequelize
    .sync({
      force: true
    })
    .then(function() {
      res.status(201).send('s-a creat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('nu s-a creat');
    });
});

app.post('/location', function(req, res) {
  Location
    .create(req.body)
    .then(function() {

      con.query("select * from locations order by id desc limit 2;", function(err, rows) {
        var location = rows[1];
        var locationNew = rows[0];
        if (location.ziDinLuna == locationNew.ziDinLuna) {
          if (distanceBetween2Locations(rows[0], rows[1]) < 0.030) {
            location.oraSfarsit = locationNew.oraInceput;

            var update = "update locations set oraSfarsit = '" + locationNew.oraInceput + "' where id = " + location.id + ";";
            con.query(update, function(err, rows, fields) {
              console.warn(update);
            })

            var deleteCommand = "delete from locations " + " where id = " + locationNew.id + ";";
            con.query(deleteCommand, function(err, rows, fields) {
              console.warn(deleteCommand);
            })
          }
        }
      });
      res.status(201).send('creat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.post('/product', function(req, res) {
  Product
    .create(req.body)
    .then(function() {
      res.status(201).send('creat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/products', function(req, res) {
  Product
    .findAll({
      attributes: ['id', 'name', 'quantity', 'idUser', 'idLocation']
    })
    .then(function(quantity) {
      res.status(200).send(quantity);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/products/:idUser', function(req, res) {
  Product
    .findAll({
      attributes: ['id', 'name', 'quantity', 'idUser', 'idLocation'],
      where: {
        idUser: req.params.idUser
      }
    })
    .then(function(product) {
      res.status(200).send(product);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/productsLocation/:idLocation', function(req, res) {
  Product
    .findAll({
      attributes: ['id', 'name', 'quantity', 'idUser', 'idLocation'],
      where: {
        idLocation: req.params.idLocation
      }
    })
    .then(function(product) {
      res.status(200).send(product);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/product/:id', function(req, res) {
  Product
    .find({
      attributes: ['id', 'name', 'quantity', 'idUser', 'idLocation'],
      where: {
        id: req.params.id
      }
    })
    .then(function(product) {
      res.status(200).send(product);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/productsName/:name', function(req, res) {
  Product
    .findAll({
      attributes: ['id', 'name', 'quantity', 'idUser', 'idLocation'],
      where: {
        name: req.params.name
      }
    })
    .then(function(product) {
      res.status(200).send(product);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.post('/user', function(req, res) {
  User
    .create(req.body)
    .then(function() {
      res.status(201).send('creat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/histories/:idUser', function(req, res) {
  History
    .findAll({
      attributes: ['id', 'loc', 'produs', 'ora', 'data', 'idUser'],
      where: {
        idUser: req.params.idUser
      }
    })
    .then(function(history) {
      res.status(200).send(history);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/user/:email', function(req, res) {
  User
    .find({
      attributes: ['id', 'email', 'name', 'imgPic', 'token'],
      where: {
        email: req.params.email
      }
    })
    .then(function(user) {
      res.status(200).send(user);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/userId/:idUser', function(req, res) {
  User
    .find({
      attributes: ['id', 'email', 'name', 'imgPic', 'token'],
      where: {
        id: req.params.idUser
      }
    })
    .then(function(user) {
      res.status(200).send(user);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/users', function(req, res) {
  User
    .findAll({
      attributes: ['id', 'email', 'name', 'imgPic', 'token']
    })
    .then(function(users) {
      res.status(200).send(users);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.post('/history', function(req, res) {
  History
    .create(req.body)
    .then(function() {
      res.status(201).send('creat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/locations', function(req, res) {
  Location
    .findAll({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser']
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/allLocations/:month/:dayOfMonth', function(req, res) {
  Location
   .findAll({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser'],
      where: {
        luna: req.params.month,
        ziDinLuna: req.params.dayOfMonth
      }
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});
app.get('/locations/:idUser', function(req, res) {
  Location
    .findAll({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser'],
      where: {
        idUser: req.params.idUser
      }
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/location/:id', function(req, res) {
  Location
    .find({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser'],
      where: {
        id: req.params.id
      }
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/locations/:month/:dayOfMonth/:idUser', function(req, res) {
  Location
    .findAll({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser'],
      where: {
        luna: req.params.month,
        ziDinLuna: req.params.dayOfMonth,
        idUser: req.params.idUser
      }
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.get('/locations/:day/:idUser', function(req, res) {
  Location
    .findAll({
      attributes: ['id', 'zi', 'luna', 'ziDinLuna', 'oraInceput', 'oraSfarsit', 'lat', 'lgn', 'idUser'],
      where: {
        zi: req.params.day,
        idUser: req.params.idUser
      }
    })
    .then(function(location) {
      res.status(200).send(location);
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.delete('/location/:id', function(req, res) {
  Location
    .find({
      where: {
        id: req.params.id
      }
    })
    .then((location) => {
      return location.destroy();
    })
    .then(function() {
      res.status(201).send('sters');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.delete('/product/:name', function(req, res) {
  Product
    .find({
      where: {
        name: req.params.name
      }
    })
    .then((product) => {
      return product.destroy();
    })
    .then(function() {
      res.status(201).send('sters');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.put('/location/:id', function(req, res) {
  Location
    .find({
      where: {
        id: req.params.id
      }
    })
    .then(function(location) {
      return location.updateAttributes(req.body);
    })
    .then(function() {
      res.status(201).send('modificat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.put('/user/:id', function(req, res) {
  User
    .find({
      where: {
        id: req.params.id
      }
    })
    .then(function(user) {
      return user.updateAttributes(req.body);
    })
    .then(function() {
      res.status(201).send('modificat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

app.put('/product/:id', function(req, res) {
  Product
    .find({
      where: {
        id: req.params.id
      }
    })
    .then(function(product) {

      console.log(req.body.idLocation);

      return product.updateAttributes(req.body);
    })
    .then(function() {
      res.status(201).send('modificat');
    })
    .catch(function(error) {
      console.warn(error);
      res.status(500).send('eroare');
    });
});

var uniqLocations = [];
var frequencyLocations;
var procent;
var date = new Date();

//notificare
var schedule = require('node-schedule');
var rule = new schedule.RecurrenceRule();
rule.minute = 07;
//rule.hour = 18;

var idUser;
var contor = 0;
var dataUserss = [];
var j = schedule.scheduleJob(rule, function() {
  client.get(SRV + '/users', function(dataUsers) {
    dataUserss = dataUsers;
    /*
    for (var i = 0; i < dataUsers.length; i++) {
      dataUserss.push(dataUsers[i].id);
    }
    */
    startNotification(contor);
  });
});

function startNotification(id) {
  procent = 0;
  frequencyLocations = new Map();
  var max = 0;
  uniqLocations = [];

  var dd = date.getDay();
  if (dd == 6) {
    dd = 0;
  }
  else {
    dd++;
  }

  uniqLocations = [];
  if (dataUserss[id].token != null || dataUserss[id].token != " ") {

    idUser = dataUserss[id].id;

    console.log(idUser + "    ##########################################################################################");

    client.get(SRV + '/locations/' + parseInt(dd) + '/' + idUser, function(data) {
      //console.log(data);

      for (var i = 0; i < data.length - 1; i++) {
        var location1 = {
          id: data[i].id,
          lat: data[i].lat,
          lgn: data[i].lgn
        };
        for (var j = i + 1; j < data.length; j++) {
          var location2 = {
            id: data[j].id,
            lat: data[j].lat,
            lgn: data[j].lgn
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
          for (var loc2 in data) {
            if (areInTheSamePlace(uniqLocations[loc1], data[loc2])) {
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

          if(uniqLocations.length != 0) {
            getLocationWithFrequenciMax();
          }
        }, millisecondsToWait2);
      }, millisecondsToWait);

    });
  }
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
  var R = 6371;
  var dLat = deg2rad(loc2.lat - loc1.lat);
  var dLon = deg2rad(loc2.lgn - loc1.lgn);
  var a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(deg2rad(loc1.lat)) * Math.cos(deg2rad(loc2.lat)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  var d = R * c;

  return d;
}

function deg2rad(deg) {
  return deg * (Math.PI / 180)
}

function getLocationWithFrequenciMax() {
  var max = 0;
  var locTmp;

  console.log(frequencyLocations.length);
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
    //console.log(actual_JSON);
    //console.log(actual_JSON.results.length);

    var nearbyPlaceRestaurant = actual_JSON.results;
    var restaurat = nearbyPlaceRestaurant[0];

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
    var details = "Cu o probabilitate de " + (max / procent * 100) + "%, maine vei fi aici: " + locTmp.lat + ", " + locTmp.lgn +
      " \n Nu ai vrea sa mananci la noi, " + restaurat.name + ", avem un raiting bun: " + restaurat.rating +
      "\n Ne gasesti pe strada " + restaurat.vicinity;
    console.log(details);

    pushNotification(restaurat.vicinity, restaurat.name, restaurat.rating, locTmp.lat, locTmp.lgn, (max / procent * 100));

  }, locTmp.lat, locTmp.lgn);
}

function loadJSON(callback, loc1, loc2) {
  var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
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

var admin = require("firebase-admin");
var serviceAccount = require("./licenta-642ad-firebase-adminsdk-cbd50-c5750ecbcb.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

function pushNotification(vicinityRestaurant, nameRestaurant, ratingRestaurant, latitude, longitude, probability) {
  client.get(SRV + '/userId/' + idUser, function(data) {
    client.get(SRV + '/products/' + idUser, function(dataProducts) {

      var produs = "carnaciori";
      var max = 0;

      console.log("lungime array produse: " + dataProducts.length);
      for (var i = 0; i < dataProducts.length; i++) {
        if (dataProducts[i].quantity > max) {
          produs = dataProducts[i].name;
          max = dataProducts[i].quantity;
        }
      }

      var registrationToken = data.token;

      var payload = {
        notification: {
          body: "Avem oferta la " + produs,
          title: nameRestaurant + ", are ceva pentru tine!",
          sound: "default",
          priority: "high"
        },
        data: {
          adresaRest: vicinityRestaurant,
          nameRest: nameRestaurant,
          ratingRest: ratingRestaurant + "",
          lat: latitude + "",
          lgn: longitude + "",
          prob: probability + ""
        }
      };

      admin.messaging().sendToDevice(registrationToken, payload)
        .then(function(response) {
          console.log("Successfully sent message:", response);
          contor++;
          if (contor < dataUserss.length) {
            startNotification(contor);
          }
        })
        .catch(function(error) {
          console.log("Error sending message:", error);
          contor++;
          if (contor < dataUserss.length) {
            startNotification(contor);
          }
        });
    });
  });
}

//stergere locatii nesemnificative
var scheduleInsignificant = require('node-schedule');
var ruleInsignificant = new scheduleInsignificant.RecurrenceRule();
ruleInsignificant.minute = 59;
ruleInsignificant.second = 59;

var j3 = schedule.scheduleJob(ruleInsignificant, function() {
  console.log("~~~~~~~~~~~~~~~~~~~~~~~~~~ Sterge locatii nesemnificative ~~~~~~~~~~~~~~~~~~~~~~~~");
  var month = date.getUTCMonth() + 1;
  var day = date.getUTCDate();

  client.get(SRV + '/allLocations/' + month + "/" + day, function(data) {
    for (var i = 0; i < data.length; i++) {
      var referinta = {
        id: data[i].id,
        zi: data[i].zi,
        luna: data[i].luna,
        ziDinLuna: data[i].ziDinLuna,
        oraInceput: data[i].oraInceput,
        oraSfarsit: data[i].oraSfarsit,
        lat: data[i].lat,
        lgn: data[i].lgn,
        idUser: data[i].idUser
      };
      console.log("minute: " + minutesLocation(referinta));
      if (minutesLocation(referinta) < 11) {
        client.delete(SRV + '/location/' + referinta.id, function(data, response) {});
      }
    }
  });
});

function updateLocation(location) {
  var args = {
    data: {
      "id": location.id,
      "zi": location.zi,
      "luna": location.luna,
      "ziDinLuna": location.ziDinLuna,
      "oraInceput": location.oraInceput,
      "oraSfarsit": location.oraSfarsit,
      "lat": location.lat,
      "lgn": location.lgn,
      "idUser": location.idUser
    },
    path: {
      "id": location.id
    },
    headers: {
      "Content-Type": "application/json"
    },
  };
  client.put(SRV + '/location/${id}', args, function(data, response) {

  });
}

function minutesLocation(location) {
  var minutes = 0;
  var hourStart = parseInt(location.oraInceput.split(":")[0]);
  var minutesStart = parseInt(location.oraInceput.split(":")[1]);
  var hourFinish = parseInt(location.oraSfarsit.split(":")[0]);
  var minutesFinish = parseInt(location.oraSfarsit.split(":")[1]);

  if (hourFinish == hourStart) {
    minutes += minutesFinish - minutesStart;
  }
  else {
    if ((minutesFinish == 0 && minutesStart != 0) || minutesStart > minutesFinish) {
      hourFinish--;
    }

    if (minutesStart < minutesFinish) {
      minutes += minutesFinish - minutesStart;
    }
    else if (minutesStart > minutesFinish) {
      minutes += 60 - minutesStart + minutesFinish;
    }
  }
  return minutes + (hourFinish - hourStart) * 60;
}

app.listen(8080);