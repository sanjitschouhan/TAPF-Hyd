let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
let gcs = require('@google-cloud/storage')();
let path = require('path');
let os = require('os');
let fs = require('fs');
let csv = require('csvtojson');

let companiesDB = admin.database().ref("/companies");
let statesDB = admin.database().ref("/states");
let bucket = gcs.bucket("gs://tapf-hyd.appspot.com");

function parseCompanyValues(jsonObj) {
    jsonObj['PAIDUP_CAPITAL (RS)'] = parseFloat(jsonObj['PAIDUP_CAPITAL (RS)']);
    return jsonObj;
}

function generateDB() {
    let filePath = "companies.csv";
    let tempFilePath = path.join(os.tmpdir(), filePath);
    let file = bucket.file(filePath);
    let states = {};
    let companies = {};
    return file.download({
        destination: tempFilePath
    }).then(() => {
        return csv()
            .fromFile(tempFilePath)
            .on("json", (jsonObj) => {
                if (states.hasOwnProperty(jsonObj.REGISTERED_STATE)) {
                    states[jsonObj.REGISTERED_STATE] += 1;
                }
                else {
                    states[jsonObj.REGISTERED_STATE] = 1;
                    companies[jsonObj.REGISTERED_STATE] = {}
                }
                companies[jsonObj.REGISTERED_STATE][jsonObj.CIN] = parseCompanyValues(jsonObj);
            }).on("done", (error) => {
                fs.unlinkSync(tempFilePath);
                for (let state in states) {
                    statesDB.child(state).set(states[state]);
                    companiesDB.child(state).set(companies[state]);
                }
            });
    });
}

exports.generateDatabaseManually = functions.https.onRequest((request, response) => {
    return generateDB().then(() => {
        return response.send("Completed");
    });
});

exports.generateDatabase = functions.storage.object().onChange((event) => {
    return generateDB()
});

