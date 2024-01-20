
fetch("location.txt")
    .then((res) => res.text())
    .then((text) => {
        buildMap(text);
    })
    .catch((e) => console.error(e));



function buildMap(coordinates) {

    let centerCoordinates = coordinates.split(",");

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

    let marker = new L.Marker(centerCoordinates, iconOptions);
    marker.addTo(map);
}