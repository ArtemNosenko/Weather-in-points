import QtQuick 2.9
import QtQuick.Layouts 1.3
import QtQuick.Controls 2.2
import QtQml 2.2
import QtQuick.Controls.Material 2.0

ItemDelegate {
    id : root
    width: parent.width
    checkable: true

    property bool selectionMode
    readonly property int currentIndex: ListView.view.currentIndex

    signal editClicked

    onClicked: {
        if (selectionMode)
            control.checked = !control.checked
        //If clicked when Edit and Info buttons shown - should hide them
        if (ListView.view.currentIndex !== index)
            ListView.view.currentIndex = index
        else
            ListView.view.currentIndex = -1
    }
    onPressAndHold: {
        control.checked = true }

    onSelectionModeChanged: {
        if (selectionMode === false)
            control.checked = false
    }

    contentItem: ColumnLayout{

        RowLayout{
            ColumnLayout{
                spacing: 0
                Label{
                    id: dateLbl
                    readonly property date pointDate: new Date(model.year, model.month,model.date, model.hour,model.minute)
                    text: pointDate.toLocaleTimeString(window.locale,Locale.ShortFormat)
                    onPointDateChanged:  notificationTimer.setNotificationTimer()
                }
                Label{
                    text: model.pointName
                }
            }

            Item{ Layout.fillWidth: true  }

            Text {
                id: temperature
                text:  model.temp + ' C°'
            }

            Image {
                id: icon
                source: model.icon
                sourceSize.width: 50
            }


            Switch{
                id: swith
                visible: !selectionMode
                checked: model.activated
                Layout.alignment: Qt.AlignTop
                onCheckedChanged: model.activated = swith.checked

            }

            CheckBox{
                id: control
                visible: selectionMode
                onCheckedChanged: {
                    model.selected = control.checked
                    model.selected  ? listView.selectedPointsCount ++ : listView.selectedPointsCount --
                }

            }

            Button{
                id: edit
                text:  "Edit"
                visible:  currentIndex === model.index && !selectionMode
                onClicked: editClicked()
            }
        }

        Flow{
            visible: edit.visible
            Layout.fillWidth: true
            anchors.margins: 4
            spacing: 10

            Text { text: qsTr("Description: " + model.weatherDescription)  }
            Text { text: qsTr("Feels like: " + model.feelsLike + ' C°')  }
            Text { text: qsTr("Pressure: " + model.pressure * 0.750062 + ' mm Hg')} //hPa to mm Hg
            Text { text: qsTr("Humidity: " + model.humidity + ' %')  }
            Text { text: qsTr("Cloudiness: " + model.clouds  + ' %' ) }
            Text { text: qsTr("Visibility: " + model.visibility + ' m') }
            Text {text:  qsTr("Wind speed: " + model.windSpeed + ' m/s') }
            Text {visible: model.windDeg !== -1;  text: qsTr("Wind deg: " + model.windDeg + ' degrees') }
            Text {visible: model.rainVolume !== -1; text: qsTr("Precipitation volume: " + model.rainVolume + ' mm') }
            Text {visible: model.snowVolume !== -1;  text: qsTr("Snow volume: " + model.snowVolume + ' mm')  }
            Text { text: qsTr("Dew Point: " + model.dewPoint   + ' C°') }
}

        Flow{
            visible:  model.activated
            Layout.fillWidth: true

            Repeater{
                id: dayRepeater
                model: daysToRepeat
                delegate: RoundButton{
                    text: Qt.locale().dayName(model.dayOfWeek,Locale.NarrowFormat)
                    flat : true
                    checked: model.repeat
                    checkable: true
                    Material.background: checked ? Material.accent : "transparent"
                    onToggled: { model.repeat = checked
                        //qtAndroidService.sendToService("test")
                    }

                }
            }
        }


    }

Timer{
    id: notificationTimer
    function setNotificationTimer(){

        const curDate = new Date()
        var dateToNotification = dateLbl.pointDate
        dateToNotification.setFullYear(curDate.getFullYear())
        dateToNotification.setMonth(curDate.getMonth())
        dateToNotification.setDate(curDate.getDate())
        //hanf hour before actual pointTime
        dateToNotification.setMinutes(dateToNotification.getMinutes() - 30)
        if (curDate > dateToNotification)
            dateToNotification.setDate(dateToNotification.getDate() + 1)

        interval = dateToNotification - curDate
        console.log(interval)
        start()



    }

    onTriggered: {

        var curDay = new Date().getDay()
        //Week day order
        if (curDay !== 0)
            curDay = curDay - 1
        else
            curDay = 6

        if ( daysToRepeat.get(curDay).repeat)
        {
            listModel.updateInfoAboutPoint(model.index)
            //qtAndroidService.sendToService(model.pointName)
        }

        setNotificationTimer()
    }
}

}


//            Label{
//                text: lat
//                //  validator: RegExpValidator { regExp: /^(\+|-)?(?:90(?:(?:\.0{1,2})?)|(?:[0-9]|[1-8][0-9])(?:(?:\.[0-9]{1,2})?))$/ }
//                //  color: acceptableInput ? 'green' : 'red'
//            }
//            Label{
//                text: lon
//                onTextChanged: console.log(dateLbl.pointDate)
//                //  validator: RegExpValidator {  regExp: /^(\+|-)?(?:180(?:(?:\.0{1,2})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\.[0-9]{1,2})?))$/}
//                //  color: acceptableInput ? 'green' : 'red'
//            }

//                indicator: Rectangle {
//                    width: 26; height: 26
//                    x: control.leftPadding
//                    y: parent.height / 2 - height / 2
//                    radius: 50
//                    border.color: "gray"
//                    border.width: 1

//                    Rectangle {
//                        width: 14; height: 14
//                        x: 6; y: 6
//                        radius: 50
//                        color:  "black"
//                        visible: control.checked
//                    }
//                }
