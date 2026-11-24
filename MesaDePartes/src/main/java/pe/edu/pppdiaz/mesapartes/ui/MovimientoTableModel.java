package pe.edu.pppdiaz.mesapartes.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.edu.pppdiaz.mesapartes.modelo.Movimiento;
import pe.edu.pppdiaz.mesapartes.util.Fechas;

/** Modelo de tabla del historial de un expediente. */
public class MovimientoTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNAS = {"Fecha", "Movimiento", "Detalle", "Usuario"};
    private List<Movimiento> datos = new ArrayList<>();

    public void setDatos(List<Movimiento> datos) {
        this.datos = datos == null ? new ArrayList<>() : datos;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return datos.size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int c) { return COLUMNAS[c]; }

    @Override
    public Object getValueAt(int fila, int col) {
        Movimiento m = datos.get(fila);
        switch (col) {
            case 0: return Fechas.paraVista(m.getFecha());
            case 1: return m.getTipo();
            case 2: return m.getDetalle();
            default: return m.getUsuario();
        }
    }
}
