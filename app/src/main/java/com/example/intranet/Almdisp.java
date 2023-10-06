package com.example.intranet;

public class Almdisp {
    String Alm_id,Usr_id;


    public Almdisp(String usr_perm_mov_id_almacen, String usr_id) {

        Alm_id = usr_perm_mov_id_almacen;
        Usr_id = usr_id;

    }

    public String getAlm_id() {
        return Alm_id;
    }

    public void setAlm_id(String usr_perm_mov_id_almacen){ Alm_id = usr_perm_mov_id_almacen; }

    public String getUsr_id() {
        return Usr_id;
    }

    public void setUsr_id(String usr_id){ Usr_id = usr_id; }
}
