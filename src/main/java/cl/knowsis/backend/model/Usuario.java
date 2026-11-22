package cl.knowsis.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 20)  private String telefono;
    @Column(length = 80)  private String region;
    @Column(length = 80)  private String comuna;
    @Column(length = 300) private String direccion;
    @Column(length = 12)  private String rut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoUsuario estado = EstadoUsuario.Activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void alCrear() { if (creadoEn == null) creadoEn = LocalDateTime.now(); }

        public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
}