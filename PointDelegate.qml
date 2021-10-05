import QtQuick 2.9
import QtQuick.Layouts 1.3
import QtQuick.Controls 2.2
import QtQml 2.2
import QtQuick.Controls.Material 2.0

ItemDelegate {
    id : root
    width: parent.width
    checkable: true

    property bool selectionMode: false
    onClicked: {
        if (selectionMode)
            control.checked = !control.checked
    }

    onPressAndHold:  control.checked = true

    onSelectionModeChanged: {
        if (selectionMode === false)
            control.checked = false
    }

    // onClicked: ListView.view.currentIndex = index

    contentItem: ColumnLayout{

        RowLayout{
            ColumnLayout{
                spacing: 0
                Label{
                    id: dateLbl
                    readonly property date pointDate: new Date(model.year, model.month,model.day, model.hour,model.minute)
                    text: pointDate.toLocaleTimeString(window.locale,Locale.ShortFormat)
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
            Text {
                id: description
                text: model.weatherDescription
            }


            Switch{
                id: swith
                visible: !selectionMode
                checked: model.activated
                Layout.alignment: Qt.AlignTop
                onCheckedChanged: model.activated = checked

            }

            CheckBox{
                id: control
                visible: selectionMode
                onCheckedChanged: {
                    model.selected = checked
                    checked ? listView.selectedPointsCount ++ : listView.selectedPointsCount --
                }

            }

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
                    onToggled:  model.repeat = checked
                }
            }
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
