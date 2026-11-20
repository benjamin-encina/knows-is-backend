package cl.knowsis.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true, length = 40)
    private String slug;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "en_menu", nullable = false)
    private boolean enMenu;

    protected Categoria(){

    }

    public Long getId() {
        return id;
    }

    public String getSlug(){
        return slug;
    }

    public String getNombre(){
        return nombre;
    }

    public boolean isEnMenu() {
        return enMenu;
    }
}
