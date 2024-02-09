
buildMap([51.508, -0.11]);



function buildMap(coordinates) {

    let centerCoordinates = coordinates;

    let mapOptions = {
        center:centerCoordinates,
        zoom:12
    }

    let map = new L.map('map' , mapOptions);

    let layer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
    map.addLayer(layer);

    let customIcon = {
        iconUrl:"../media/placeholder.png",
        iconSize:[40,40]
    }
    let myIcon = L.icon(customIcon);

    let iconOptions = {
        title:"apartment",
        draggable:false,
        icon:myIcon
    }

    /*const marker = L.marker([51.5, -0.09]).addTo(map)
        .bindPopup('<b>Hello world!</b><br />I am a popup.').openPopup();

    /*const circle = L.circle([51.508, -0.11], {
        color: 'red',
        fillColor: '#f03',
        fillOpacity: 0.5,
        radius: 500
    }).addTo(map).bindPopup('I am a circle.');*/

    var circle = L.circleMarker([51.508, -0.11], {
        color: 'red',
        fillColor: '#f03',
        fillOpacity: 0.5,
        radius: 100
    }).addTo(map);

    /*let circle0 = L.circle([51.508, -0.09], {
        color: 'red',
        fillColor: '#63d27f',
        fillOpacity: 0.5,
        radius: 100
    }).addTo(map);*/

    var circle1 = L.circleMarker([51.508, -0.09], {
        color: 'red',
        fillColor: '#019fe1',
        fillOpacity: 0.5,
        radius: 100
    }).addTo(map);

    // .bindPopup("I am a circle.");


    //var circle = L.circle([51.508, -0.11]).addTo(map);

    var polygon = L.polygon([
        [51.509, -0.08],
        [51.503, -0.06],
        [51.51, -0.047]
    ]);

    polygon.setStyle({fillColor: '#019fe1', color: '#000ce8', fillOpacity: 0.7});

    polygon.addTo(map);


    //let marker = new L.Marker(centerCoordinates, iconOptions);
    //marker.addTo(map);

    var myZoom = {
        start:  map.getZoom(),
        end: map.getZoom()
    };

    map.on('zoomstart', function(e) {
        myZoom.start = map.getZoom();
    });

    map.on('zoomend', function(e) {
        myZoom.end = map.getZoom();
        var diff = myZoom.start - myZoom.end;
        if (diff > 0) {
            circle.setRadius(circle.getRadius() * 1.1); //2);
        } else if (diff < 0) {
            circle.setRadius(circle.getRadius() / 1.1); //2);
        }
    });
}