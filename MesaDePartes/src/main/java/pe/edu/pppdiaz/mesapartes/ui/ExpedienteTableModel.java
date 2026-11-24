package pe.edu.pppdiaz.mesapartes.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.edu.pppdiaz.mesapartes.modelo.Expediente;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/** Modelo de tabla reutilizado en Registro, Derivación, Consulta y Reportes. */
public class ExpedienteTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    public static final String[] COLUMNAS = {
        "Código", "Fecha registro", "Remitente", "DNI", "Tipo", "Asunto", "Folios", "Estado", "Ubicación"
    };

    private List<Expediente> datos = new ArrayList<>();

    public void setDatos(List<Expediente> datos) {
        this.datos = datos == null ? new ArrayList<>() : datos;
        fireTableDataChanged();
    }

    public List<Expediente> getDatos() { return datos; }

    public Expediente get(int fila) { return datos.get(fila); }

    /** Devuelve la fila (índice del modelo) del expediente con ese id, o -1. */
    public int indiceDe(long id) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId() == id) return i;
        }
        return -1;
    }

    @Override public int getRowCount() { return datos.size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int c) { return COLUMNAS[c]; }

    @Override
    public Object getValueAt(int fila, int col) {
        Expediente e = datos.get(fila);
        switch (col) {
            case 0: return e.getCodigo();
            case 1: return Fechas.paraVista(e.getFechaRegistro());
            case 2: return e.getRemitente().getNombre();
            case 3: return e.getRemitente().getDocumento();
            case 4: return e.getTipoDocumento();
            case 5: return e.getAsunto();
            case 6: return e.getFolios();
            case 7: return e.getEstado();
            default: return e.getDependenciaActual();
        }
    }

    @Override
    public Class<?> getColumnClass(int c) { return c == 6 ? Integer.class : String.class; }
}
