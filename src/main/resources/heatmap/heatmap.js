fetch("locations.txt")
    .then((res) => res.text())
    .then((text) => {
        // Dividi il testo in righe utilizzando il carattere di nuova riga come separatore
        const lines = text.split('\n');
        // Rimuovi eventuali spazi bianchi in eccesso da ciascuna riga
        const cleanLines = lines.map(line => line.trim());
        // Passa l'array di righe alla funzione buildMap
        buildMap(cleanLines);
    }).catch((e) => console.error(e));

// Funzione per costruire la mappa
function buildMap(rows) {
    let max = 0;
    let maxCoordinates = [];

    // leggo le coordinate per trovare il centro (sarà quello col max counter)
    for(let i = 0; i < rows.length; i++) {
        let readValue = rows[i].split(";");
        // splitto la prima parte in due stringhe
        let centerCoordinates = readValue[0].split(",");
        let counter = parseInt(readValue[1]); // Converte counter in un numero
        if(counter > max) {
            max = counter;
            maxCoordinates = centerCoordinates;
        }
    }

    let mapOptions = {
        center:maxCoordinates,
        zoom:10,
        minZoom:10,
        maxZoom:15
    }

    let map = new L.map('map' , mapOptions);

    let layer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
    map.addLayer(layer);

    // aggiungo i cerchi alla map con colori diversi in base al counter
    for(let i = 0; i < rows.length; i++) {

        let readValue = rows[i].split(";");
        // splitto la prima parte in due stringhe
        let centerCoordinates = readValue[0].split(",");
        let counter = readValue[1];

        let color = '';
        if(counter < max/3)
             color = 'green';
        else if(counter >= max/3 && counter < max/3*2)
             color = 'yellow';
        else if(counter >= max/3*2)
             color = 'red';

        let opacity = counter * 1/max + 0.2;
        if(opacity >= 0.5)
            opacity = 0.5;

        let circle = L.circleMarker(centerCoordinates, {
            color: "transparent",
            fillColor: color,
            fillOpacity: opacity,
            radius : 10
        }).addTo(map).bindPopup("Number of apartments in this area : " + counter);

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
            var scale = 2;
            if (diff < 0) {
                circle.setRadius(circle.getRadius() * scale);
            } else if (diff > 0) {
                circle.setRadius(circle.getRadius() / scale);
            }
        });

    }
}