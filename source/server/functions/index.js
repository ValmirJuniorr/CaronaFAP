var functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.addMessage = functions.https.onRequest((req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push it into the Realtime Database then send a response
  admin.database().ref('/messages').push({original: original}).then(snapshot => {
    // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
    //res.redirect(303, snapshot.ref);
    res.send("Hello World!");
    console.log("hello World!")
  });
});

exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
.onWrite(event => {
      // Grab the current value of what was written to the Realtime Database.
      const original = event.data.val();
      console.log('Uppercasing', event.params.pushId, original);
      const uppercase = original.toUpperCase();
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      return event.data.ref.parent.child('uppercase').set(uppercase);
    });

exports.findPatner= functions.https.onRequest((req, res) =>{
    var idRide = req.query.idRide;
    var ref = functions.database.ref('/rides/'+idRide);
    ref.once('value').then(function(snapshot) {
      res.status(200).json(snapshot)
  });
})

exports.date = functions.https.onRequest((req, res) => {
  var id = req.query.id
  admin.database().ref('/rides/' + id).once('value').then(function(snapshot) {
    var response = snapshot.val
    console.log(response)
    res.send(response+" isso que é bacana");
  });
});

