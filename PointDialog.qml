import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick 2.9
import QtQuick.Window 2.3

Dialog {
    id: pointDlg
    title: "Add new point"
    modal: true
    standardButtons: DialogButtonBox.Ok | DialogButtonBox.Cancel

    property  ListModel lModel
    function formatNumber(number){
        return number < 10 && number >= 0 ? "0" + number : number.toString()
    }

    onRejected: pointDlg.close()
    onAccepted: { 
        var today = new Date()
        lModel.append({
                          "lat": mapdialog.pointCoordinate.x,
                          "lon": mapdialog.pointCoordinate.y,
                          "year": today.getFullYear(),
                          "month": today.getMonth(),
                          "day": today.getDate(),
                          "hour": hoursTumbler.currentIndex,
                          "minute": minutesTumbler.currentIndex,
                          "pointName": tf.text,
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
            id: tf
            placeholderText: "Point name"
        }
        }

    }

}

/*##^##
Designer {
    D{i:0;autoSize:true;height:480;width:640}
}
##^##*/
