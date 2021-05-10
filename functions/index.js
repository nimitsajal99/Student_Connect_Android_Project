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
          "Logged": true,
          "Tags": 3,
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
        users.doc(data.userName).collection("My Tags").doc("Info").set({
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
          users.doc(data.userName).collection("Tags")
              .doc(doc.data().Course).set({
                "Value": 1,
                "Inbuilt": true,
              });
          tags.doc(doc.data().Course).collection("Student")
              .doc(data.userName).set({
                "Info": "Info",
              });
        });
        const branchInfo = university.doc("Next")
            .collection(data.universityName)
            .doc("Next")
            .collection(data.collegeName)
            .doc(data.branchName);
        branchInfo.get().then((doc) => {
          // console.log("Document data:", doc.data());
          // console.log("Document data:", doc.data().Course);
          const tags = admin.firestore().collection("Tags");
          users.doc(data.userName).collection("Tags")
              .doc(doc.data().Course).set({
                "Value": 1,
                "Inbuilt": true,
              });
          tags.doc(doc.data().Course).collection("Student")
              .doc(data.userName).set({
                "Info": "Info",
              });
          tags.doc(doc.data().Level).collection("Student")
              .doc(data.userName).set({
                "Info": "Info",
              });
          users.doc(data.userName).collection("Tags")
              .doc(doc.data().Level).set({
                "Value": 1,
                "Inbuilt": true,
              });
        });
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
          const tag = admin.firestore().collection("Users").doc(data.userName)
              .collection("Tags").doc(data.tagNo1);
          tag.get().then(function(docc) {
            if (docc.exists) {
              console.log("Tag already exists", docc.id);
              tag.set({
                "Value": (docc.data().Value + 1),
                "Inbuilt": docc.data().Inbuilt,
              });
            } else {
              // doc.data() will be undefined in this case
              console.log("No such document!");
              tag.set({
                "Value": 1,
                "Inbuilt": false,
              });
            }
          });
        }
        if (data.tagNo2 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo2)
              .set({
                "Confidence": data.confidence2,
              });
          const tag = admin.firestore().collection("Users").doc(data.userName)
              .collection("Tags").doc(data.tagNo2);
          tag.get().then(function(docc) {
            if (docc.exists) {
              console.log("Tag already exists", docc.id);
              tag.set({
                "Value": (docc.data().Value + 1),
                "Inbuilt": docc.data().Inbuilt,
              });
            } else {
              // doc.data() will be undefined in this case
              console.log("No such document!");
              tag.set({
                "Value": 1,
                "Inbuilt": false,
              });
            }
          });
        }
        if (data.tagNo3 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo3)
              .set({
                "Confidence": data.confidence3,
              });
          const tag = admin.firestore().collection("Users").doc(data.userName)
              .collection("Tags").doc(data.tagNo3);
          tag.get().then(function(docc) {
            if (docc.exists) {
              console.log("Tag already exists", docc.id);
              tag.set({
                "Value": (docc.data().Value + 1),
                "Inbuilt": docc.data().Inbuilt,
              });
            } else {
              // doc.data() will be undefined in this case
              console.log("No such document!");
              tag.set({
                "Value": 1,
                "Inbuilt": false,
              });
            }
          });
        }
        if (data.tagNo4 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo4)
              .set({
                "Confidence": data.confidence4,
              });
          const tag = admin.firestore().collection("Users").doc(data.userName)
              .collection("Tags").doc(data.tagNo4);
          tag.get().then(function(docc) {
            if (docc.exists) {
              console.log("Tag already exists", docc.id);
              tag.set({
                "Value": (docc.data().Value + 1),
                "Inbuilt": docc.data().Inbuilt,
              });
            } else {
              // doc.data() will be undefined in this case
              console.log("No such document!");
              tag.set({
                "Value": 1,
                "Inbuilt": false,
              });
            }
          });
        }
        if (data.tagNo0 != "xxxxx") {
          post.doc(docRef.id).collection("Tags").doc(data.tagNo0)
              .set({
                "Confidence": data.confidence0,
              });
          const tag = admin.firestore().collection("Users").doc(data.userName)
              .collection("Tags").doc(data.tagNo0);
          tag.get().then(function(docc) {
            if (docc.exists) {
              console.log("Tag already exists", docc.id);
              tag.set({
                "Value": (docc.data().Value + 1),
                "Inbuilt": docc.data().Inbuilt,
              });
            } else {
              // doc.data() will be undefined in this case
              console.log("No such document!");
              tag.set({
                "Value": 1,
                "Inbuilt": false,
              });
            }
          });
        }
        if (data.faceId0 != "xxxxx") {
          const smile = (data.smile0 == "true" || data.smile0 == "True");
          const named = (data.named0 == "true" || data.named0 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId0)
              .set({
                "Smile": smile,
                "RotY": data.rotY0,
                "RotZ": data.rotZ0,
                "Left": data.boundLeft0,
                "Right": data.boundRight0,
                "Top": data.boundTop0,
                "Bottom": data.boundBottom0,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId0).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId1 != "xxxxx") {
          const smile = (data.smile1 == "true" || data.smile1 == "True");
          const named = (data.named1 == "true" || data.named1 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId1)
              .set({
                "Smile": smile,
                "RotY": data.rotY1,
                "RotZ": data.rotZ1,
                "Left": data.boundLeft1,
                "Right": data.boundRight1,
                "Top": data.boundTop1,
                "Bottom": data.boundBottom1,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId1).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId2 != "xxxxx") {
          const smile = (data.smile2 == "true" || data.smile2 == "True");
          const named = (data.named2 == "true" || data.named2 == "True");

          post.doc(docRef.id).collection("Faces").doc(data.faceId2)
              .set({
                "Smile": smile,
                "RotY": data.rotY2,
                "RotZ": data.rotZ2,
                "Left": data.boundLeft2,
                "Right": data.boundRight2,
                "Top": data.boundTop2,
                "Bottom": data.boundBottom2,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId2).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId3 != "xxxxx") {
          const smile = (data.smile3 == "true" || data.smile3 == "True");
          const named = (data.named3 == "true" || data.named3 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId3)
              .set({
                "Smile": smile,
                "RotY": data.rotY3,
                "RotZ": data.rotZ3,
                "Left": data.boundLeft3,
                "Right": data.boundRight3,
                "Top": data.boundTop3,
                "Bottom": data.boundBottom3,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId3).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId4 != "xxxxx") {
          const smile = (data.smile4 == "true" || data.smile4 == "True");
          const named = (data.named4 == "true" || data.named4 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId4)
              .set({
                "Smile": smile,
                "RotY": data.rotY4,
                "RotZ": data.rotZ4,
                "Left": data.boundLeft4,
                "Right": data.boundRight4,
                "Top": data.boundTop4,
                "Bottom": data.boundBottom4,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId4).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId5 != "xxxxx") {
          const smile = (data.smile5 == "true" || data.smile5 == "True");
          const named = (data.named5 == "true" || data.named5 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId5)
              .set({
                "Smile": smile,
                "RotY": data.rotY5,
                "RotZ": data.rotZ5,
                "Left": data.boundLeft5,
                "Right": data.boundRight5,
                "Top": data.boundTop5,
                "Bottom": data.boundBottom5,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId5).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId6 != "xxxxx") {
          const smile = (data.smile6 == "true" || data.smile6 == "True");
          const named = (data.named6 == "true" || data.named6 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId6)
              .set({
                "Smile": smile,
                "RotY": data.rotY6,
                "RotZ": data.rotZ6,
                "Left": data.boundLeft6,
                "Right": data.boundRight6,
                "Top": data.boundTop6,
                "Bottom": data.boundBottom6,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId6).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId7 != "xxxxx") {
          const smile = (data.smile7 == "true" || data.smile7 == "True");
          const named = (data.named7 == "true" || data.named7 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId7)
              .set({
                "Smile": smile,
                "RotY": data.rotY7,
                "RotZ": data.rotZ7,
                "Left": data.boundLeft7,
                "Right": data.boundRight7,
                "Top": data.boundTop7,
                "Bottom": data.boundBottom7,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId7).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId8 != "xxxxx") {
          const smile = (data.smile8 == "true" || data.smile8 == "True");
          const named = (data.named8 == "true" || data.named8 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId8)
              .set({
                "Smile": smile,
                "RotY": data.rotY8,
                "RotZ": data.rotZ8,
                "Left": data.boundLeft8,
                "Right": data.boundRight8,
                "Top": data.boundTop8,
                "Bottom": data.boundBottom8,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId8).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
        if (data.faceId9 != "xxxxx") {
          const smile = (data.smile9 == "true" || data.smile9 == "True");
          const named = (data.named9 == "true" || data.named9 == "True");
          post.doc(docRef.id).collection("Faces").doc(data.faceId9)
              .set({
                "Smile": smile,
                "RotY": data.rotY9,
                "RotZ": data.rotZ9,
                "Left": data.boundLeft9,
                "Right": data.boundRight9,
                "Top": data.boundTop9,
                "Bottom": data.boundBottom9,
                "Tagged": named,
              });
          if (named) {
            users.doc(data.faceId9).collection("My Tags").doc(docRef.id)
                .set({
                  "Liked": false,
                  "Time": time,
                });
          }
        }
      })
      .catch(function(error) {
        console.error("Error while uploading picture: ", error);
      });
});

exports.deletePost = functions.https.onCall((data, context) => {
  const promises = [];
  const post = admin.firestore().collection("Post");
  const users = admin.firestore().collection("Users");
  return post.doc(data.uid).collection("Faces")
      .get()
      .then((snapshot) => {
        snapshot.forEach((doc) => {
          if (doc.id != "Info" && doc.data().Tagged == true) {
            promises.push(users.doc(doc.id).collection("My Tags")
                .doc(data.uid).delete());
          }
        });
        promises.push(post.doc(data.uid).delete());
        promises.push(users.doc(data.userName).collection("My Posts")
            .doc(data.uid).delete());
        return users.doc(data.userName).collection("Friends")
            .get()
            .then((friends) => {
              friends.forEach((user) => {
                if (user.id != "Info") {
                  promises.push(users.doc(user.id).collection("My Feed")
                      .doc(data.uid).delete());
                }
              });
              return Promise.all(promises);
            });
      })
      .then((metaData) => {
        console.log("post deleted", data.uid, " metadata = ", metaData);
      })
      .catch((error) => {
        console.error("Error while uploading picture: ", error);
      });
});

exports.addTag = functions.https.onCall((data, context) => {
  const tag = admin.firestore().collection("Users").doc(data.userName)
      .collection("Tags").doc(data.tagName);
  tag.get().then(function(doc) {
    if (doc.exists) {
      console.log("Tag already exists", doc.id);
      tag.set({
        "Value": (doc.data().Value + 1),
        "Inbuilt": doc.data().Inbuilt,
      });
    } else {
      // doc.data() will be undefined in this case
      console.log("No such document!");
      tag.set({
        "Value": 1,
        "Inbuilt": false,
      });
    }
  });
});

exports.removeTag = functions.https.onCall((data, context) => {
  const tag = admin.firestore().collection("Users").doc(data.userName)
      .collection("Tags").doc(data.tagName);
  tag.get().then(function(doc) {
    if (doc.exists) {
      console.log("Tag already exists", doc.id);
      if (doc.data().Value > 1) {
        tag.set({
          "Value": (doc.data().Value - 1),
          "Inbuilt": doc.data().Inbuilt,
        });
      } else {
        tag.delete().then((doc) => {
          console.log("Tag Deleted", data.tagName);
        });
      }
    }
  });
});

