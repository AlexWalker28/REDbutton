const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.checkRequestApprovements = functions.database.ref('/Groups/{group}/requests/{request}')
    .onWrite(event => {

        const request = event.data.val();
        const userId = event.data.key;
        const groupName1 = event.data.ref.parent.parent.key;
        const usersRef = event.data.adminRef.root.child('Users');

        if (event.data.exists() && !event.data.previous.exists()) {
            //new request was added, need to add groupName to user's pending branch
            console.log("request added");
            console.log(userId, groupName1);
            return usersRef.child(userId).child('pending').child(groupName1).set(true);
            //return event.data.ref.parent.parent.parent.parent.child('Users').child(userId).child('pending').child(groupName1).set(true);
        }
        else if (!event.data.exists() && event.data.previous.exists()) {
            //request was deleted, need to delete groupName from user's pending branch
            console.log("deleting group from user's pending branch");
            console.log(userId, groupName1);
            return usersRef.child(userId).child('pending').child(groupName1).set(null);
            //return event.data.ref.parent.parent.parent.parent.child('Users').child('pending').child(groupName1).set(null);
        } else {
            //need to check request approvements
            console.log("need to check request now");
            var approvementsCount = request.agreeCount;
            const groupRef = event.data.ref.parent.parent;

            return groupRef.once("value")
                .then(function(snapshot){
                    const requiredAmountOfApprovals = snapshot.child("requiredAmountOfApprovals").val();

                    if (approvementsCount >= requiredAmountOfApprovals){
                        //need to add user to members and delete request
                        console.log('need to add user to members');
                        return groupRef.child('members').child(userId).set(true)
                                .then(() => {
                                    console.log('user added to members, deleting request');
                                    groupRef.child('requests').child(userId).set(null);
                                    console.log('request deleted');
                                });
                    } else {
                        //still not enough approvements
                        console.log('still not enough approvements');
                        return;
                    }
                });

        }
    });

exports.groupMemberListener = functions.database.ref('/Groups/{group}/members/{member}')
    .onWrite( event => {
        const userId = event.data.key;
        const groupName = event.data.ref.parent.parent.key;
        console.log(userId, groupName);
        const userGroupMembershipRef = admin.database().ref('Users').child(userId).child('groups').child(groupName);

        if (event.data.exists() && !event.data.previous.exists()) {
            console.log("On member added");
            return userGroupMembershipRef.set(true);
        }
        else if (!event.data.exists() && event.data.previous.exists()) {
            console.log("On member deleted");
            return userGroupMembershipRef.set(null);
        } else {
            return;
        }
    });

exports.countApprovements= functions.database.ref('Groups/{group}/requests/{request}/approved/{userId}')
    .onWrite( event => {
        console.log("countApprovements triggered");
        const countRef = event.data.ref.parent.parent.child('agreeCount');

        return countRef.transaction(current => {
            if (event.data.exists() && !event.data.previous.exists()) {
                console.log("need to increase the number of approvements");
                return (current || 0) + 1;
            }
            else {
                if (current == null) {
                console.log("current == null");
                return;
                } else if (!event.data.exists() && event.data.previous.exists()) {
                    console.log("need to reduce the number of approvements");
                    return (current || 0) - 1;
                }
            }
          }).then(() => {
            console.log('Counter updated.');
          });

    });
