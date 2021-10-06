import QtQuick.Controls 2.14
import QtPositioning 5.14
import QtQuick 2.0
import QtQuick.Window 2.14
import QtLocation 5.11
Item {

    id: pointDlg
    readonly property point pointCoordinate: Qt.point(marker.coordinate.latitude, marker.coordinate.longitude)

    PositionSource {
        id: userPos
        updateInterval: 100000
        active: true
    }


    Map {
        id: mapView
        anchors.fill: parent
        plugin:  Plugin {
            name: "osm"

            PluginParameter {
                    name: "osm.mapping.providersrepository.disabled"
                    value: "true"
                }
                PluginParameter {
                    name: "osm.mapping.providersrepository.address"
                    value: "http://maps-redirect.qt.io/osm/5.6/"
                }
        }

        center: userPos.position.coordinate
        zoomLevel: 14

        MapQuickItem {
              id:marker
              sourceItem: Image{
                  id: image
                  source: "marker.png"
                  width: 40
                  height: 80
              }
              coordinate: mapView.center
              anchorPoint.x: image.width/2
              anchorPoint.y: image.height
          }


    }

}
