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
    property point coord
    function formatNumber(number){
        return number < 10 && number >= 0 ? "0" + number : number.toString()
    }

    onRejected: pointDlg.close()
    onAccepted: { 
        var today = new Date()
        lModel.append({
                          "lat": coord.x,
                          "lon": coord.y,
                          "year": today.getFullYear(),
                          "month": today.getMonth(),
                          "day": today.getDate(),
                          "hour": hoursTumbler.currentIndex,
                          "minute": minutesTumbler.currentIndex,
                          "pointName": tf.text,
                          "activated": false,
                          "selected" : false,
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
        RowLayout{
            id : rowTumbler

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
            TextField{
                id: tf
                placeholderText: "Point name"
            }
        }

    }

}
