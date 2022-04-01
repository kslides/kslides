let circumferenceReducer = (c, planet) => {
    return c + planet.diameter * Math.PI;
}

let planets = [
    {name: 'mars', diameter: 6779},
    {name: 'earth', diameter: 12742},
    {name: 'jupiter', diameter: 139820}
]

let c = planets.reduce(circumferenceReducer, 0)