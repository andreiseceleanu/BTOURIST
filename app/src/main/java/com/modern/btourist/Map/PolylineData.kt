package com.modern.btourist.Map

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg

class PolylineData(polyline: Polyline, leg: DirectionsLeg) {
    var polyline:Polyline
    var leg:DirectionsLeg
    init{
        this.polyline = polyline
        this.leg = leg
    }
    public override fun toString():String {
        return ("PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}'.toString())
    }
}