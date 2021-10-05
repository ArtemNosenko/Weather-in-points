import QtQuick 2.9
import QtQuick.Window 2.3
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.0

import io.qt.backendnetworking 1.0

ApplicationWindow {
    id: window
    visible: true
    width: 640
    height: 480
    title: qsTr("Points")
    property bool initialyLoadedData: false

    SaveLoadData{
        id: saveLoadData
        lModel: listModel
    }
    Component.onCompleted: {
        saveLoadData.loadData()
        initialyLoadedData = true
    }
    onClosing: saveLoadData.saveData()


    BackEndNetworking{
        id: backEnd
        host: "api.openweathermap.org"
        request: "/data/2.5/onecall?lat=55.75&lon=37.88&exclude=daily&appid=491a54922af0f56f87b30ee988483263"
    }

    Timer {
           interval: 3600000; running: true; repeat: true
           onTriggered: listModel.updateInfoAboutPoints()
       }

    ListView {
        id: listView
        property bool selectionMode: false
        property int selectedPointsCount: 0

        onSelectionModeChanged: {
            if (selectionMode === false)
                selectedPointsCount = 0
        }

        anchors.top: columnLayout.bottom
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.left: parent.left

        onCountChanged:{
            if (initialyLoadedData)
                saveLoadData.saveData()
            else
                initialyLoadedData = true
        }
        model: ListModel {
            id : listModel

            function updateInfoAboutPoint(number){
                backEnd.request = "/data/2.5/onecall?lat=" + get(number).lat + '&lon=' +
                        get(number).lon +  "&exclude=daily&appid=491a54922af0f56f87b30ee988483263"

                var JsonString = backEnd.run()
                var JsonObject = JSON.parse(JsonString)

                var closetstDateDataNumber = 0
                var dateForRequest = new Date()
                dateForRequest.setHours(get(number).hour)
                dateForRequest.setMinutes(get(number).minute)

                if (dateForRequest < new Date())
                    dateForRequest.setDate(dateForRequest.getDate() + 1)

                var minDateDiff = Math.abs(dateForRequest - new Date(JsonObject.hourly[0].dt*1000))

                for (var j = 1; j < JsonObject.hourly.length; j++)
                {
                     var curDataDiff = Math.abs(dateForRequest - new Date(JsonObject.hourly[j].dt*1000))
                      if (minDateDiff > curDataDiff)
                      {
                          closetstDateDataNumber = j
                          minDateDiff = curDataDiff
                      }

                }

                get(number).icon = "http://openweathermap.org/img/wn/" +  JsonObject.hourly[closetstDateDataNumber].weather[0].icon + "@2x.png"
                get(number).weatherDescription = JsonObject.hourly[closetstDateDataNumber].weather[0].description
                get(number).temp = (parseInt (JsonObject.hourly[closetstDateDataNumber].temp) - 273).toString() //K to C
            }

            function updateInfoAboutPoints() {
                for(var i = 0; i < count; i++)
                updateInfoAboutPoint(i)
            }



        }
        delegate: PointDelegate {
            onPressAndHold: listView.selectionMode = true
            selectionMode: listView.selectionMode

        }
    }

    RoundButton {
        id: addButton
        text: qsTr("+")
        visible: !listView.selectionMode
        anchors.bottom: listView.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        onPressed:  mapDialog.open()

    }
    Button {
        id: delButton
        enabled: listView.selectedPointsCount != 0
        text: "Delete"
        visible: listView.selectionMode
        anchors.bottom: listView.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        onPressed:  deleteDialog.open()

    }

    ColumnLayout {
        id: columnLayout
        spacing: 0

        visible: listView.selectionMode
        width: window.width
        height: visible ? 50 : 0

        RoundButton {
            id: roundButton
            y: 0
            text: "X"
            Layout.fillWidth: false
            Layout.columnSpan: 0
            Layout.rowSpan: 0
            Layout.preferredHeight: 0
            Layout.preferredWidth: 0
            padding: 0

            onPressed: listView.selectionMode = false
        }
        Text {
            id: selectedCount
            height: roundButton.height
            text: "Selected: " + listView.selectedPointsCount
            color: Material.foreground
            Layout.alignment:   Qt.AlignHCenter
        }
    }

    MapDialog{
        id: mapDialog
        width: window.width
        height: window.height
        visible: false
        onAccepted: pointDialog.open()
    }

    PointDialog{
        id: pointDialog
        coord: mapDialog.pointCoordinate
        x: Math.round((parent.width - width) / 2)
        y: Math.round((parent.height - height) / 2)
        lModel: listView.model
    }

    Dialog {
        id: deleteDialog
        title: "Delete selected points?"
        modal: true
        width: 300

        x: Math.round((parent.width - width) / 2)
        y: Math.round((parent.height - height) / 2)

        standardButtons: DialogButtonBox.Ok | DialogButtonBox.Cancel

        onAccepted: {
            var indexOffset = 0
            for (var i = 0; i < listModel.count; i++)
            {
                if (listModel.get(i).selected)
                {
                    listModel.remove(i,1)
                    i--
                }
            }
            listView.selectionMode = false
        }
        onRejected: close()

    }

}

