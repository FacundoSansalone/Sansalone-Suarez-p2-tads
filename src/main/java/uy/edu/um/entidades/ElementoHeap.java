package uy.edu.um.entidades;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ElementoHeap<T> implements Comparable<ElementoHeap<T>> {
    private T valor;
    private float prioridad;

    @Override
    public int compareTo(ElementoHeap<T> otro) {

        return Float.compare(otro.prioridad, this.prioridad);
    }
}
