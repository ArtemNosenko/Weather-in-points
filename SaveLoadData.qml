import QtQuick 2.0

import QtQuick.LocalStorage 2.0


Item {
    property  ListModel lModel
    function loadData() {
        var db = LocalStorage.openDatabaseSync("MapWeatherPoints", "1.0", "Weather data at a specific points and time", 1000000);
        db.transaction(
                    function(tx) {
                       // tx.executeSql(' DROP TABLE Points');
                        tx.executeSql('CREATE TABLE IF NOT EXISTS Points(point TEXT, daysToRepeat TEXT,id TEXT)');

                        var rs = tx.executeSql('SELECT * FROM Points');

                        for (var i = 0; i < rs.rows.length; i++) {

                            var objPoint = JSON.parse(rs.rows.item(i).point)
                            var daysToRepeat = JSON.parse(rs.rows.item(i).daysToRepeat)

                            for (var j = 0; j < daysToRepeat.length; j++)
                                daysToRepeat[j] =  JSON.parse(daysToRepeat[j])
                            var id = rs.rows.item(i).id;
                            lModel.append({
                                              "lat":       objPoint["lat"],
                                              "lon":       objPoint["lon"],
                                              "year":      objPoint["year"],
                                              "month":     objPoint["month"],
                                              "date":       objPoint["date"],
                                              "hour":      objPoint["hour"],
                                              "minute":    objPoint["minute"],
                                              "pointName": objPoint["pointName"],
                                              "activated": objPoint["activated"],
                                              "temp" : objPoint["temp"],
                                              "selected" : false,
                                              "icon" :     objPoint["icon"],
                                              "weatherDescription": objPoint["weatherDescription"],
                                              "feelsLike": objPoint["feelsLike"],
                                              "pressure": objPoint["pressure"],
                                              "humidity":objPoint["humidity"],
                                              "dewPoint":objPoint["dewPoint"],
                                              "clouds":objPoint["clouds"],
                                              "visibility":objPoint["visibility"],
                                              "windSpeed":objPoint["windSpeed"],
                                              "windDeg":objPoint["windDeg"],
                                              "rainVolume":objPoint["rainVolume"],
                                              "snowVolume":objPoint["snowVolume"],
                                              "windGust" : objPoint["windGust"],
                                              "id": id,
                                              "daysToRepeat" : daysToRepeat
                                          }
                                          )


                            lModel.updateInfoAboutPoint(lModel.count - 1)
                        }
                    }
                    )
    }


    function saveData(){
        var db = LocalStorage.openDatabaseSync("MapWeatherPoints", "1.0", "Weather data at a specific points and time", 1000000);
        db.transaction(
                    function(tx) {
                        tx.executeSql('CREATE TABLE IF NOT EXISTS Points(point TEXT, daysToRepeat TEXT,id TEXT )');
                        tx.executeSql('DELETE FROM Points');

                        for (var i = 0; i < lModel.count; i++)
                        {
                            var array = []
                            for (var j = 0; j < 7; j++) {
                                array.push(JSON.stringify(lModel.get(i).daysToRepeat.get(j)))
                            }

                            tx.executeSql('INSERT INTO Points VALUES(?,?,?)',[JSON.stringify(lModel.get(i)), JSON.stringify(array),lModel.get(i).id]);
                        }
                    }
                    )
    }

}
