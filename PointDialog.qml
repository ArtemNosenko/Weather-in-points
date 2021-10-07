import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick 2.9
import QtQuick.Window 2.3

Dialog {
    id: pointDlg
    title: mode === 0 ?  "Add new point" : "Edit point"
    modal: true
    //0 - append mode, 1 - edit  mode
    property int mode: 0
    property int editedIndex: -1

    onEditedIndexChanged: {
        if (editedIndex != -1 && mode === 1){
            var pointObj = lModel.get(editedIndex)
            mapdialog.pointCoordinate.x =  pointObj.lat
            mapdialog.pointCoordinate.y =  pointObj.lon
            pointNameField.text = pointObj.pointName
            hoursTumbler.currentIndex = pointObj.hour
            minutesTumbler.currentIndex = pointObj.minute
        }
    }

    standardButtons: mode === 0 ? DialogButtonBox.Apply |  DialogButtonBox.Ok | DialogButtonBox.Cancel : DialogButtonBox.Ok |  DialogButtonBox.Cancel

    property  ListModel lModel
    function formatNumber(number){
        return number < 10 && number >= 0 ? "0" + number : number.toString()
    }

    onRejected: pointDlg.close()

    function createCurrentPoint(){
        var today = new Date()
        lModel.append({
                          "lat": mapdialog.pointCoordinate.x,
                          "lon": mapdialog.pointCoordinate.y,
                          "year": today.getFullYear(),
                          "month": today.getMonth(),
                          "day": today.getDate(),
                          "hour": hoursTumbler.currentIndex,
                          "minute": minutesTumbler.currentIndex,
                          "pointName": pointNameField.text,
                          "activated": false,
                          "selected" : false,
                          "temp" : '',
                          "icon" :'',
                          "weatherDescription": '',
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
            lModel.setProperty(editedIndex, "lat", mapdialog.pointCoordinate.x)
            lModel.setProperty(editedIndex, "lon", mapdialog.pointCoordinate.y)
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

    contentItem: RowLayout{

        MapDialog{
            id: mapdialog
            Layout.fillWidth: true
            Layout.fillHeight: true
        }
        ColumnLayout{

            Layout.leftMargin: 30
            RowLayout{

                Layout.leftMargin: 20
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
                placeholderText: "Point name"
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
