import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick 2.9
import QtQuick.Window 2.3

Dialog {
    id: pointDlg
    title: mode === 0 ?  "Add new point" : "Edit point"
    modal: true

    property  ListModel lModel
    //0 - append mode, 1 - edit  mode
    property int mode: 0
    property int editedIndex: -1

    onEditedIndexChanged: {
        if (editedIndex != -1 && mode === 1){
            var pointObj = lModel.get(editedIndex)

            mapdialog.markerCoordinate.latitude =  pointObj.lat
            mapdialog.markerCoordinate.longitude =  pointObj.lon
            pointNameField.text = pointObj.pointName
            hoursTumbler.currentIndex = pointObj.hour
            minutesTumbler.currentIndex = pointObj.minute
        }
    }

    standardButtons: mode === 0 ? DialogButtonBox.Apply |  DialogButtonBox.Ok | DialogButtonBox.Cancel : DialogButtonBox.Ok |  DialogButtonBox.Cancel

    function formatNumber(number){
        return number < 10 && number >= 0 ? "0" + number : number.toString()
    }

    onRejected: pointDlg.close()

    function createCurrentPoint(){
        var today = new Date()
        lModel.append({
                          "lat": mapdialog.markerCoordinate.latitude,
                          "lon": mapdialog.markerCoordinate.longitude,
                          "year": today.getFullYear(),
                          "month": today.getMonth(),
                          "date": today.getDate(),
                          "hour": hoursTumbler.currentIndex,
                          "minute": minutesTumbler.currentIndex,
                          "pointName": pointNameField.text,
                          "activated": false,
                          "selected" : false,
                          "temp" : 0,
                          "icon" :'',
                          "weatherDescription": '',
                          "feelsLike": 0,
                          "pressure": 0,
                          "humidity":0,
                          "dewPoint":0,
                          "clouds":0,
                          "visibility":0,
                          "windSpeed":0,
                          "windDeg":0,
                          "windGust":-1,
                          "rainVolume":-1,
                          "snowVolume":-1,
                          "id": new Date().getMilliseconds(),
                          "daysToRepeat" : [
                              {"dayOfWeek": 1, "repeat" : false},
                              {"dayOfWeek": 2, "repeat" : false},
                              {"dayOfWeek": 3, "repeat" : false},
                              {"dayOfWeek": 4, "repeat" : false},
                              {"dayOfWeek": 5, "repeat" : false},
                              {"dayOfWeek": 6, "repeat" : false},
                              {"dayOfWeek": 0, "repeat" : false},
                          ]
                      }
                      )
        lModel.updateInfoAboutPoint(lModel.count - 1)
    }

    onAccepted: {
        if (mode === 0)
            createCurrentPoint()
        if (mode === 1)
        {
            lModel.setProperty(editedIndex, "lat", mapdialog.markerCoordinate.latitude)
            lModel.setProperty(editedIndex, "lon", mapdialog.markerCoordinate.longitude)
            lModel.setProperty(editedIndex, "hour", hoursTumbler.currentIndex)
            lModel.setProperty(editedIndex, "minute", minutesTumbler.currentIndex)
            lModel.setProperty(editedIndex, "pointName", pointNameField.text)
            lModel.updateInfoAboutPoint(editedIndex)
        }
    }
    Timer{
        id: applyStatusTimer
        interval: 1000;  repeat: false
        onTriggered: applyResult.text = ' '
    }

    onApplied: {
        createCurrentPoint()
        applyResult.text = 'Point ' + pointNameField.text + ' added'
        applyStatusTimer.start()
    }

    contentItem:  GridLayout{
        id: grid
        function isMobilePortrait(){
            return (Qt.platform.os === "android" || Qt.platform.os === "ios") && Screen.width < Screen.height
        }

        columns: isMobilePortrait() ? 1 : 2
        MapDialog{
            id: mapdialog
            Layout.fillWidth: true
            Layout.fillHeight: true
        }
        ColumnLayout{
           // Layout.leftMargin:  30
            Layout.alignment: Qt.AlignHCenter
            RowLayout{
                Layout.leftMargin:  50
                Tumbler{
                    id: hoursTumbler
                    model: 24
                    delegate: Text{ text: formatNumber(modelData) }
                }

                Tumbler{
                    id: minutesTumbler
                    model: 60
                    delegate: Text{ text: formatNumber(modelData) }
                }
            }

            TextField{
                id: pointNameField
                Layout.alignment: Qt.AlignHCenter
                placeholderText: "Point name"
                horizontalAlignment:  TextInput.AlignHCenter
            }
            Text { id: applyResult  }
        }

    }




}

/*##^##
Designer {
    D{i:0;autoSize:true;height:480;width:640}
}
##^##*/
