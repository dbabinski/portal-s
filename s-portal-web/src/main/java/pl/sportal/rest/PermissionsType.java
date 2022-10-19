/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
public enum PermissionsType {
    
    READ(4),
    ADD(3),
    DELETE(2),
    UPDATE(1);
    
    public final int position;
    
    private PermissionsType(int position) {
        this.position = position;
    }
    
    public int getPosition() {
        return position;
    }
    
    public static PermissionsType get(int position) {
        return Stream.of(PermissionsType.values())
                .collect(Collectors.toMap(PermissionsType::getPosition, Function.identity()))
                .get(position);
    }
    
}
