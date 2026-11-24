package pe.edu.pppdiaz.mesapartes.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pppdiaz.mesapartes.db.Conexion;
import pe.edu.pppdiaz.mesapartes.excepcion.MesaPartesException;
import pe.edu.pppdiaz.mesapartes.modelo.Dependencia;

public class DependenciaDAO {

    public List<Dependencia> listar() throws MesaPartesException {
        List<Dependencia> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener(); Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre FROM dependencia ORDER BY id")) {
            while (rs.next()) {
                lista.add(new Dependencia(rs.getLong("id"), rs.getString("nombre")));
            }
            return lista;
        } catch (SQLException e) {
            throw new MesaPartesException("No se pudieron cargar las dependencias: " + e.getMessage(), e);
        }
    }
}
