/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.sportal.rest;


public class Permissions {
    
    public static final int INT_READ = 8;
    public static final int INT_ADD = 4;
    public static final int INT_DELETE = 2;
    public static final int INT_UPDATE = 1;

    private boolean read = false;
    private boolean add = false;
    private boolean delete = false;
    private boolean update = false;
    
    public Permissions() {}
    
    public Permissions(Integer permissions){
        setPermissions(permissions);
    }
    
    public boolean isRead() {
        return read;
    }
    
    public Permissions setRead(boolean read) {
        this.read = read;
        return this;
    }
    
     public boolean isAdd() {
        return add;
    }
    
    public Permissions setAdd(boolean add) {
        this.add = add;
        return this;
    }
    
     public boolean isDelete() {
        return delete;
    }
    
    public Permissions setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }
    
     public boolean isUpdate() {
        return update;
    }
    
    public Permissions setUpdate(boolean update) {
        this.update = update;
        return this;
    }
    
    private void setPermissions(Integer permissions) {
        if (permissions != null) {
            read = isBitSet(permissions, PermissionsType.READ.getPosition());
            add = isBitSet(permissions, PermissionsType.ADD.getPosition());
            delete = isBitSet(permissions, PermissionsType.DELETE.getPosition());
            update = isBitSet(permissions, PermissionsType.UPDATE.getPosition());
            
        }
    }
    
    private boolean isBitSet(int number, int position) {
        int newNumber = number >> (position -1);
        return (newNumber & 1) == 1;
    }
    
    public boolean check(PermissionsType permissionsType) throws PermissionsException {
        boolean permissions = false; 
        switch (permissionsType) {
            case READ:
                permissions = read;
                break;
            case ADD:
                permissions = add;
                break;
            case DELETE:
                permissions = delete;
                break;
            case UPDATE:
                permissions = update;
                break;
        }
        if (!permissions) {
            throw new PermissionsException();
        }
        return permissions;
    }
}
