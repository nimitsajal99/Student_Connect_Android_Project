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

exports.createUser = functions.region("asia-south1")
    .https.onCall((data, context) => {
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
            users.doc(data.userName).collection("Chat Users").doc("Info").set({
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
            users.doc(data.userName).collection("Tagged Users").doc("Info")
                .set({
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

exports.uploadPost = functions.region("asia-south1")
    .https.onCall((data, context) => {
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
                      users.doc(doc.id).collection("My Feed").doc(docRef.id)
                          .set({
                            "Liked": false,
                            "Time": time,
                          });
                    }
                  });
                })
                .catch((err) => {
                  console.log("Error getting Friends", err);
                });
            if (data.tagNo0 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo0)
                  .set({
                    "Confidence": data.confidence0,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo0);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence0 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence0 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence0 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence0 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo1 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo1)
                  .set({
                    "Confidence": data.confidence1,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo1);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence1 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence1 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence1 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence1 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo2 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo2)
                  .set({
                    "Confidence": data.confidence2,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo2);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence2 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence2 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence2 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence2 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo3 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo3)
                  .set({
                    "Confidence": data.confidence3,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo3);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence3 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence3 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence3 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence3 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo4 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo4)
                  .set({
                    "Confidence": data.confidence4,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo4);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence4 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence4 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence4 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence4 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo5 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo5)
                  .set({
                    "Confidence": data.confidence5,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo5);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence5 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence5 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence5 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence5 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo6 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo6)
                  .set({
                    "Confidence": data.confidence6,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo6);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence6 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence6 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence6 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence6 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo7 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo7)
                  .set({
                    "Confidence": data.confidence7,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo7);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence7 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence7 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence7 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence7 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo8 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo8)
                  .set({
                    "Confidence": data.confidence8,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo8);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence8 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence8 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence8 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence8 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
                }
              });
            }
            if (data.tagNo9 != "xxxxx") {
              post.doc(docRef.id).collection("Tags").doc(data.tagNo9)
                  .set({
                    "Confidence": data.confidence9,
                  });
              const tag = admin.firestore().collection("Users")
                  .doc(data.userName)
                  .collection("Tags").doc(data.tagNo9);
              tag.get().then(function(docc) {
                if (docc.exists) {
                  if (data.confidence9 > 0.75) {
                    tag.set({
                      "Value": (docc.data().Value + 3),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else if (data.confidence9 > 0.60) {
                    tag.set({
                      "Value": (docc.data().Value + 2),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  } else {
                    tag.set({
                      "Value": (docc.data().Value + 1),
                      "Inbuilt": docc.data().Inbuilt,
                    });
                  }
                } else {
                  // doc.data() will be undefined in this case
                  if (data.confidence9 > 0.75) {
                    tag.set({
                      "Value": 3,
                      "Inbuilt": false,
                    });
                  } else if (data.confidence9 > 0.60) {
                    tag.set({
                      "Value": 2,
                      "Inbuilt": false,
                    });
                  } else {
                    tag.set({
                      "Value": 1,
                      "Inbuilt": false,
                    });
                  }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId0).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId0).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId0).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId0).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId1).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId1).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId1).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId1).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId2).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId2).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId2).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId2).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId3).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId3).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId3).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId3).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId4).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId4).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId4).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId4).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId5).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId5).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId5).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId5).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId6).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId6).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId6).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId6).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId7).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId7).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId7).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId7).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId8).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId8).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId8).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId8).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
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
                users.doc(data.userName).collection("Tagged Users")
                    .doc(data.faceId9).get().then(function(docc) {
                      if (docc.exists) {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId9).set({
                              "Count": (docc.data().Count + 1),
                            });
                      } else {
                        users.doc(data.userName).collection("Tagged Users")
                            .doc(data.faceId9).set({
                              "Count": 1,
                            });
                      }
                    });
                if (data.tagNo0 != "xxxxx" && data.confidence0 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo0);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo1 != "xxxxx" && data.confidence1 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo1);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo2 != "xxxxx" && data.confidence2 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo2);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo3 != "xxxxx" && data.confidence3 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo3);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo4 != "xxxxx" && data.confidence4 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo4);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo5 != "xxxxx" && data.confidence5 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo5);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo6 != "xxxxx" && data.confidence6 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo6);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo7 != "xxxxx" && data.confidence7 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo7);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo8 != "xxxxx" && data.confidence8 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo8);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
                if (data.tagNo9 != "xxxxx" && data.confidence9 > 0.60) {
                  const tag = admin.firestore().collection("Users")
                      .doc(data.faceId9).collection("Tags").doc(data.tagNo9);
                  tag.get().then(function(docc) {
                    if (docc.exists) {
                      tag.set({
                        "Value": (docc.data().Value + 1),
                        "Inbuilt": docc.data().Inbuilt,
                      });
                    } else {
                      tag.set({
                        "Value": 1,
                        "Inbuilt": false,
                      });
                    }
                  });
                }
              }
            }
          })
          .catch(function(error) {
            console.error("Error while uploading picture: ", error);
          });
    });

exports.deletePost = functions.region("asia-south1")
    .https.onCall((data, context) => {
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
                promises.push(post.doc(data.uid).collection("Faces")
                    .doc(doc.id).delete());
              } else {
                promises.push(post.doc(data.uid).collection("Faces")
                    .doc(doc.id).delete());
              }
            });
            return post.doc(data.uid).collection("Comments")
                .get()
                .then((snap) => {
                  snap.forEach((doc) => {
                    promises.push(post.doc(data.uid).collection("Comments")
                        .doc(doc.id).delete());
                  });
                  return post.doc(data.uid).collection("Tags")
                      .get()
                      .then((tagsnap) => {
                        tagsnap.forEach((doc) => {
                          promises.push(post.doc(data.uid)
                              .collection("Tags").doc(doc.id).delete());
                        });
                        promises.push(post.doc(data.uid).delete());
                        promises.push(users.doc(data.userName)
                            .collection("My Posts").doc(data.uid).delete());
                        return users.doc(data.userName).collection("Friends")
                            .get()
                            .then((friends) => {
                              friends.forEach((user) => {
                                if (user.id != "Info") {
                                  promises.push(users.doc(user.id)
                                      .collection("My Feed")
                                      .doc(data.uid).delete());
                                }
                              });
                              return Promise.all(promises);
                            });
                      });
                });
          })
          .then((metaData) => {
            console.log("post deleted", data.uid);
          })
          .catch((error) => {
            console.error("Error while uploading picture: ", error);
          });
    });

exports.addTag = functions.region("asia-south1")
    .https.onCall((data, context) => {
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
          console.log("No such tag exists");
          tag.set({
            "Value": 1,
            "Inbuilt": false,
          });
        }
      });
    });

exports.removeTag = functions
    .region("asia-south1")
    .https.onCall((data, context) => {
      const tag = admin.firestore().collection("Users").doc(data.userName)
          .collection("Tags").doc(data.tagName);
      tag.get().then(function(doc) {
        if (doc.exists) {
          if (doc.data().Value > 1) {
            console.log("Tag decremented", doc.id);
            tag.set({
              "Value": (doc.data().Value - 1),
              "Inbuilt": doc.data().Inbuilt,
            });
          } else {
            console.log("Tag deleted");
            tag.delete().then((doc) => {
            });
          }
        }
      });
    });

exports.likePost = functions.region("asia-south1")
    .https.onCall((data, context) => {
      if (data.tagNo0 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo0);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo1 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo1);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo2 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo2);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo3 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo3);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo4 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo4);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo5 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo5);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo6 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo6);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo7 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo7);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo8 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo8);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
      if (data.tagNo9 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo9);
        tag.get().then(function(docc) {
          if (docc.exists) {
            tag.set({
              "Value": (docc.data().Value + 1),
              "Inbuilt": docc.data().Inbuilt,
            });
          } else {
            // doc.data() will be undefined in this case
            tag.set({
              "Value": 1,
              "Inbuilt": false,
            });
          }
        });
      }
    });

exports.dislikePost = functions.region("asia-south1")
    .https.onCall((data, context) => {
      if (data.tagNo0 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo0);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo1 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo1);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo2 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo2);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo3 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo3);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo4 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo4);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo5 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo5);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo6 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo6);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo7 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo7);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo8 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo8);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
      if (data.tagNo9 != "xxxxx") {
        const tag = admin.firestore().collection("Users").doc(data.userName)
            .collection("Tags").doc(data.tagNo9);
        tag.get().then(function(doc) {
          if (doc.exists) {
            if (doc.data().Value > 1) {
              tag.set({
                "Value": (doc.data().Value - 1),
                "Inbuilt": doc.data().Inbuilt,
              });
            } else {
              tag.delete().then((doc) => {
              });
            }
          }
        });
      }
    });
