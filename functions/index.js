const functions = require("firebase-functions");

// The Firebase Admin SDK to access Firestore.
const admin = require("firebase-admin");
// const {user} = require("firebase-functions/lib/providers/auth");
admin.initializeApp();

// https://github.com/firebase/snippets-node/blob/
// a0f415f99bcc28f8bad4ff8e86b93337e94efc1d/
// firestore/main/index.js#L541-L549

// exports.addUserToFirestore = functions.auth.user().onCreate((user) => {
//   const userRef = admin.firestore().collection("test");
//   return userRef.doc(user.uid).set({
//     emailId: user.email,
//   });
// });

exports.createUser = functions.https.onCall((data, context) => {
  const phone = "+91" + data.phoneNumber;
  return admin.auth().createUser({
    email: data.email,
    phoneNumber: phone,
    password: data.password,
    displayName: data.name,
  })
      .then((userRecord) => {
        const d = new Date();
        const n1 = d.getTime();
        console.log("Successfully created new user:", userRecord.uid);
        const userTable = admin.firestore().collection("User Table");
        userTable.doc(userRecord.uid).set({
          Username: data.userName,
        });
        const users = admin.firestore().collection("Users");
        users.doc(data.userName).set({
          "Branch": data.branchName,
          "College": data.collegeName,
          "Description": "",
          "Email": data.email,
          "Name": data.name,
          "Phone Number": data.phoneNumber,
          "Picture": data.url,
          "Semester": data.semesterName,
          "University": data.universityName,
        });
        users.doc(data.userName).collection("Chats").doc("Info").set({
          "Info": "Info",
        });
        users.doc(data.userName).collection("Friends").doc("Info").set({
          "Info": "Info",
        });
        users.doc(data.userName).collection("My Posts").doc("Info").set({
          "Info": "Info",
        });
        users.doc(data.userName).collection("My Feed").doc("Info").set({
          "Info": "Info",
        });
        users.doc(data.userName).collection("Tags").doc("Info").set({
          "Info": "Info",
        });
        users.doc(data.userName).collection("Medals").doc(data.collegeName)
            .set({
              "Info": "Info",
            });
        users.doc(data.userName).collection("Medals").doc(data.branchName)
            .set({
              "Info": "Info",
            });
        const year = "Year - " + data.semesterName;
        users.doc(data.userName).collection("Medals").doc(year)
            .set({
              "Info": "Info",
            });
        const university = admin.firestore().collection("University");
        university.doc(data.universityName).collection("Student")
            .doc(data.userName).set({
              "Info": "Info",
            });
        university.doc("Next").collection(data.universityName)
            .doc(data.collegeName).collection("Student")
            .doc(data.userName).set({
              "Info": "Info",
            });
        university.doc("Next").collection(data.universityName)
            .doc("Next").collection(data.collegeName)
            .doc(data.branchName).collection("Student")
            .doc(data.userName).set({
              "Info": "Info",
            });
        university.doc("Next").collection(data.universityName)
            .doc("Next").collection(data.collegeName)
            .doc("Next").collection(data.branchName)
            .doc(data.semesterName).collection("Student")
            .doc(data.userName).set({
              "Info": "Info",
            });
        const course = university.doc("Next")
            .collection(data.universityName)
            .doc(data.collegeName);
        course.get().then((doc) => {
          // console.log("Document data:", doc.data());
          // console.log("Document data:", doc.data().Course);
          const tags = admin.firestore().collection("Tags");
          tags.doc(doc.data().Course).collection("Student")
              .doc(data.userName).set({
                "Info": "Info",
              });
        });

        const d2 = new Date();
        const n2 = d2.getTime();
        const n = n2-n1;
        console.log("n1 = ", n1);
        console.log("n2 = ", n2);
        console.log("n = ", n);
      })
      .catch((error) => {
        console.log("Error creating new user:", error);
      });
});

exports.uploadPost = functions.https.onCall((data, context) => {
  const post = admin.firestore().collection("Post");
  const time = admin.firestore.FieldValue.serverTimestamp();
  post.add({
    "Description": data.description,
    "Dp": data.dp,
    "From": data.userName,
    "Likes": 0,
    "Picture": data.picture,
    "Time": time,
    "Faces": data.noOfFaces,
    "Tags": data.noOfTags,
    "Emotion": data.smile,
  })
      .then(function(docRef) {
        console.log("Picture uploaded: ", docRef.id);
        post.doc(docRef.id).collection("Comments").doc("Info")
            .set({
              "Info": "Info",
            });
        post.doc(docRef.id).collection("Tags").doc("Info")
            .set({
              "Info": "Info",
            });
        post.doc(docRef.id).collection("Faces").doc("Info")
            .set({
              "Info": "Info",
            });
        const users = admin.firestore().collection("Users");
        users.doc(data.userName).collection("My Posts").doc(docRef.id)
            .set({
              "Liked": false,
              "Time": time,
            });
        users.doc(data.userName).collection("Friends")
            .get()
            .then((snapshot) => {
              snapshot.forEach((doc) => {
                if (doc.id != "Info") {
                  console.log("Uploading picture in ", doc.id, " Feed");
                  users.doc(doc.id).collection("My Feed").doc(docRef.id)
                      .set({
                        "Liked": false,
                        "Time": time,
                      });
                }
              });
            })
            .catch((err) => {
              console.log("Error getting documents", err);
            });
        if (data.tagNo1 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo1)
              .set({
                "Confidence": data.confidence1,
              });
        }
        if (data.tagNo2 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo2)
              .set({
                "Confidence": data.confidence2,
              });
        }
        if (data.tagNo3 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo3)
              .set({
                "Confidence": data.confidence3,
              });
        }
        if (data.tagNo4 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo4)
              .set({
                "Confidence": data.confidence4,
              });
        }
        if (data.tagNo0 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo0)
              .set({
                "Confidence": data.confidence0,
              });
        }
        if (data.faceId0 != "xxxxx") {
          const smile = (data.smile0 == "true" || data.smile0 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId0)
              .set({
                "Smile": smile,
                "RotY": data.rotY0,
                "RotZ": data.rotZ0,
                "Bound": data.bound0,
              });
        }
        if (data.faceId1 != "xxxxx") {
          const smile = (data.smile1 == "true" || data.smile1 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId1)
              .set({
                "Smile": smile,
                "RotY": data.rotY1,
                "RotZ": data.rotZ1,
                "Bound": data.bound1,
              });
        }
        if (data.faceId2 != "xxxxx") {
          const smile = (data.smile2 == "true" || data.smile2 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId2)
              .set({
                "Smile": smile,
                "RotY": data.rotY2,
                "RotZ": data.rotZ2,
                "Bound": data.bound2,
              });
        }
        if (data.faceId3 != "xxxxx") {
          const smile = (data.smile3 == "true" || data.smile3 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId3)
              .set({
                "Smile": smile,
                "RotY": data.rotY3,
                "RotZ": data.rotZ3,
                "Bound": data.bound3,
              });
        }
        if (data.faceId4 != "xxxxx") {
          const smile = (data.smile4 == "true" || data.smile4 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId4)
              .set({
                "Smile": smile,
                "RotY": data.rotY4,
                "RotZ": data.rotZ4,
                "Bound": data.bound4,
              });
        }
        if (data.faceId5 != "xxxxx") {
          const smile = (data.smile5 == "true" || data.smile5 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId5)
              .set({
                "Smile": smile,
                "RotY": data.rotY5,
                "RotZ": data.rotZ5,
                "Bound": data.bound5,
              });
        }
        if (data.faceId6 != "xxxxx") {
          const smile = (data.smile6 == "true" || data.smile6 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId6)
              .set({
                "Smile": smile,
                "RotY": data.rotY6,
                "RotZ": data.rotZ6,
                "Bound": data.bound6,
              });
        }
        if (data.faceId7 != "xxxxx") {
          const smile = (data.smile7 == "true" || data.smile7 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId7)
              .set({
                "Smile": smile,
                "RotY": data.rotY7,
                "RotZ": data.rotZ7,
                "Bound": data.bound7,
              });
        }
        if (data.faceId8 != "xxxxx") {
          const smile = (data.smile8 == "true" || data.smile8 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId8)
              .set({
                "Smile": smile,
                "RotY": data.rotY8,
                "RotZ": data.rotZ8,
                "Bound": data.bound8,
              });
        }
        if (data.faceId9 != "xxxxx") {
          const smile = (data.smile9 == "true" || data.smile9 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId9)
              .set({
                "Smile": smile,
                "RotY": data.rotY9,
                "RotZ": data.rotZ9,
                "Bound": data.bound9,
              });
        }
      })
      .catch(function(error) {
        console.error("Error while uploading picture: ", error);
      });
});
