const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.countapprovementchange = functions.database.ref('/Users/{userid}/approvements/{approvementid}').onWrite(event => {
  const collectionRef = event.data.ref.parent;
  const countRef = collectionRef.parent.child('approvements_count');

  // Return the promise from countRef.transaction() so our function 
  // waits for this async event to complete before it exits.
  return countRef.transaction(current => {
    if (event.data.exists() && !event.data.previous.exists()) {
      return (current || 0) + 1;
    }
    else if (!event.data.exists() && event.data.previous.exists()) {
      return (current || 0) - 1;
    }
  }).then(() => {
    console.log('Counter updated.');
  });
});

// If the number of likes gets deleted, recount the number of likes
exports.recountapprovements = functions.database.ref('/approvements/{approvementid}/approvements_count').onWrite(event => {
  if (!event.data.exists()) {
    const counterRef = event.data.ref;
    const collectionRef = counterRef.parent.child('approvements');
    
    // Return the promise from counterRef.set() so our function 
    // waits for this async event to complete before it exits.
    return collectionRef.once('value')
        .then(messagesData => counterRef.set(messagesData.numChildren()));
  }
});