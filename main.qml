import QtQuick 2.9
import QtQuick.Window 2.3
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.12
import QtQuick.Controls.Material 2.0

import io.qt.backendnetworking 1.0

ApplicationWindow {
    id: window
    visible: true
    width: 640
    height: 480
    title: qsTr("Points")
    property bool initialyLoadedData: false

    function updateWeatherDatabase(){
        //listModel.updateInfoAboutPoints()
       // saveLoadData.saveData()
    }

    SaveLoadData {
        id: saveLoadData
        lModel: listModel
    }
    Component.onCompleted: {
        saveLoadData.loadData()
        initialyLoadedData = true
    }
    onClosing: saveLoadData.saveData()

    BackEndNetworking {
        id: backEnd
        host: "api.openweathermap.org"
    }

    Timer {
        interval: 3600000
        running: true
        repeat: true
        onTriggered: listModel.updateInfoAboutPoints()
    }


    ListView {
        id: listView
        property bool selectionMode: false
        property int selectedPointsCount: 0

        onSelectionModeChanged: currentIndex = -1

        anchors.top: selectedItemsInfoColumn.bottom
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.left: parent.left

        onCountChanged: {
            if (initialyLoadedData)
                saveLoadData.saveData()
            else
                initialyLoadedData = true
        }
        model: ListModel {
            id: listModel

            function updateInfoAboutPoint(number) {
                backEnd.request = "/data/2.5/onecall?lat=" + get(
                            number).lat + '&lon=' + get(
                            number).lon + "&exclude=daily&appid=491a54922af0f56f87b30ee988483263"

                var JsonString = backEnd.run()
                var JsonObject = JSON.parse(JsonString)

                var closetstDateDataNumber = 0
                var dateForRequest = new Date()
                dateForRequest.setHours(get(number).hour)
                dateForRequest.setMinutes(get(number).minute)

                if (dateForRequest < new Date())
                    dateForRequest.setDate(dateForRequest.getDate() + 1)

                var minDateDiff = Math.abs(
                            dateForRequest - new Date(JsonObject.hourly[0].dt * 1000))

                for (var j = 1; j < JsonObject.hourly.length; j++) {
                    var curDataDiff = Math.abs(
                                dateForRequest - new Date(JsonObject.hourly[j].dt * 1000))
                    if (minDateDiff > curDataDiff) {
                        closetstDateDataNumber = j
                        minDateDiff = curDataDiff
                    }
                }
                setProperty(number, "icon", "http://openweathermap.org/img/wn/"
                            + JsonObject.hourly[closetstDateDataNumber].weather[0].icon + "@2x.png")
                setProperty(number, "weatherDescription",
                            JsonObject.hourly[closetstDateDataNumber].weather[0].description)
                setProperty(number, "temp", parseInt(
                                JsonObject.hourly[closetstDateDataNumber].temp) - 273) //K to C
                setProperty(number, "feelsLike", parseInt(
                                JsonObject.hourly[closetstDateDataNumber].feels_like) - 273)
                setProperty(number, "pressure",
                            JsonObject.hourly[closetstDateDataNumber].pressure)
                setProperty(number, "humidity",
                            JsonObject.hourly[closetstDateDataNumber].humidity)
                setProperty(number, "dewPoint", parseInt(
                                JsonObject.hourly[closetstDateDataNumber].dew_point) - 273)
                setProperty(number, "clouds",
                            JsonObject.hourly[closetstDateDataNumber].clouds)
                setProperty(number, "visibility",
                            JsonObject.hourly[closetstDateDataNumber].visibility)
                setProperty(number, "windSpeed",
                            JsonObject.hourly[closetstDateDataNumber].wind_speed)
                setProperty(number, "windDeg",
                            JsonObject.hourly[closetstDateDataNumber].wind_deg)
                if (JsonObject.hourly[closetstDateDataNumber].rain !== undefined)
                    setProperty(number, "rainVolume",
                                JsonObject.hourly[closetstDateDataNumber].rain)
                if (JsonObject.hourly[closetstDateDataNumber].snow !== undefined)
                    setProperty(number, "snowVolume",
                                JsonObject.hourly[closetstDateDataNumber].snow)
                if (JsonObject.hourly[closetstDateDataNumber].wind_gust !== undefined)
                    setProperty(number, "windGust",
                                JsonObject.hourly[closetstDateDataNumber].wind_gust)


            }

            function updateInfoAboutPoints() {
                for (var i = 0; i < count; i++)
                    updateInfoAboutPoint(i)
            }
        }

        delegate: PointDelegate {
            onPressAndHold: listView.selectionMode = true
            selectionMode: listView.selectionMode

            onEditClicked: {
                pointDialog.mode = 1
                pointDialog.editedIndex = listView.currentIndex
                pointDialog.open()
            }
        }
        //Initial state - items not selected
        Component.onCompleted: currentIndex = -1
    }

    RoundButton {
        id: addButton
        text: qsTr("+")
        visible: !listView.selectionMode
        anchors.bottom: listView.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        onPressed: {
            pointDialog.mode = 0
            pointDialog.editedIndex = -1
            pointDialog.open()
        }
    }
    Button{
        id:test
        text: "test"
        anchors.bottom: listView.bottom
        anchors.left: addButton.right
        onClicked: qtAndroidService.notify("title", new Date().getSeconds())
    }
    Button {
        id: delButton
        enabled: listView.selectedPointsCount != 0
        text: "Delete"
        visible: listView.selectionMode
        anchors.bottom: listView.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        onPressed: deleteDialog.open()
    }

    ColumnLayout {
        id: selectedItemsInfoColumn
        spacing: 0
        // @disable-check M16
        visible: listView.selectionMode
        // @disable-check M16
        width: window.width
        // @disable-check M16
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
            Layout.alignment: Qt.AlignHCenter
        }
    }

    PointDialog {
        id: pointDialog
        width: window.width
        height: window.height
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
            for (var i = 0; i < listModel.count; i++) {
                if (listModel.get(i).selected) {
                    listModel.remove(i, 1)
                    i--
                }
            }
            listView.selectionMode = false
        }
        onRejected: close()
    }
}
