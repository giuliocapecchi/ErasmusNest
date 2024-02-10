
buildMap([51.508, -0.11]);



function buildMap(coordinates) {

    let centerCoordinates = coordinates;

    let mapOptions = {
        center:centerCoordinates,
        zoom:12,
        //minZoom:20,
        //maxZoom:5,
    }

    let map = new L.map('map' , mapOptions);

    let layer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
    map.addLayer(layer);


    var circle = L.circleMarker([51.508, -0.11], {
        color: 'red',
        fillColor: '#f03',
        fillOpacity: 0.5,
        radius: 100
    }).addTo(map);

    var circle1 = L.circleMarker([51.508, -0.09], {
        color: 'blue',
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
        var scale = 1.5;
        if (diff > 0) {
            circle.setRadius(circle.getRadius() * scale); //2);
        } else if (diff < 0) {
            circle.setRadius(circle.getRadius() / scale); //2);
        }
    });
}