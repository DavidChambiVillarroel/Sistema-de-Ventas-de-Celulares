import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Celular {
    private String marca;
    private String modelo;
    private int precio;
    private int stock;

    public Celular(String marca, String modelo, int precio, int stock) {
        this.marca = marca; 
        this.modelo = modelo;
        this.precio = precio; 
        this.stock = stock;
    }

    public void consultarInformacion() {
        System.out.println("Marca: " + marca);
        System.out.println("Modelo: " + modelo);
        System.out.println("Precio: " + precio);
        System.out.println("Stock: " + stock);
    }

    public void actualizarStock(int cantidadVendida) {
        if (cantidadVendida <= stock) {
            stock -= cantidadVendida;
            System.out.println("Stock actualizado. Nuevo stock: " + stock);
        } else {
            System.out.println("No hay suficiente stock para vender " + cantidadVendida + " unidades.");
        }
    }

    public int getPrecio() { return precio; }
    public int getStock()  { return stock; }
    public String getModelo() { return modelo; }
    public String getMarca()  { return marca; }
}

/*  
PATRÓN DE COMPORTAMIENTO: STRATEGY
 
Propósito: definir una FAMILIA de algoritmos (formas de pago), encapsular cada 
uno y hacerlos INTERCAMBIABLES. El cliente (TiendaCelulares) usa solo la interfaz 
MetodoPago y desconoce la implementación concreta. Permite añadir PagoQR, PagoCripto,
PagoTransferencia... sin modificar al cliente (Open/Closed).

Participantes:
    - Strategy: MetodoPago (interfaz)
    - ConcreteStrategy: PagoEfectivo, PagoTarjeta, PagoQR
    - Context: TiendaCelulares (usa la estrategia)
*/

interface MetodoPago {
    void pagar();
    String getNombreMetodo();
    int getRecargoPorcentaje();
}

class PagoEfectivo implements MetodoPago {
    @Override 
    public void pagar() { 
        System.out.println("Pago realizado en efectivo"); 
    }
    @Override 
    public String getNombreMetodo() { return "Efectivo"; }
    @Override 
    public int getRecargoPorcentaje() { return 0; }
}

class PagoTarjeta implements MetodoPago {
    @Override 
    public void pagar() { 
        System.out.println("Pago realizado con tarjeta"); 
    }
    @Override 
    public String getNombreMetodo() { return "Tarjeta"; }
    @Override 
    public int getRecargoPorcentaje() { return 5; }
}

class PagoQR implements MetodoPago {
    private final String codigoQR;

    public PagoQR(String codigoQR) { 
        this.codigoQR = codigoQR; 
    }
    @Override 
    public void pagar() {
        System.out.println("Pago realizado vía QR (código: " + codigoQR + ")");
    }
    @Override 
    public String getNombreMetodo() { return "QR"; }
    @Override 
    public int getRecargoPorcentaje() { return 2; }
}

/*
PATRÓN CREACIONAL: FACTORY METHOD

Propósito: encapsular la creación de objetos MetodoPago en fábricas concretas, 
de modo que Main no conozca las clases concretas (PagoEfectivo, PagoTarjeta, PagoQR).
Main elige qué ConcreteCreator instanciar y llama a su crearMetodoPago(). 
El cliente no conoce las clases concretas de MetodoPago; solo la interfaz.

Participantes:
    - Product: MetodoPago (interfaz)
    - ConcreteProduct: PagoEfectivo, PagoTarjeta, PagoQR
    - Creator: MetodoPagoFactory (interfaz)
    - ConcreteCreator: PagoEfectivoFactory, PagoTarjetaFactory, PagoQRFactory
*/
interface MetodoPagoFactory {
    MetodoPago crearMetodoPago();
}

class PagoEfectivoFactory implements MetodoPagoFactory {
    @Override 
    public MetodoPago crearMetodoPago() { return new PagoEfectivo(); }
}

class PagoTarjetaFactory implements MetodoPagoFactory {
    @Override 
    public MetodoPago crearMetodoPago() { return new PagoTarjeta(); }
}

class PagoQRFactory implements MetodoPagoFactory {
    private final String codigoQR;
    public PagoQRFactory(String codigoQR) { this.codigoQR = codigoQR; }
    @Override 
    public MetodoPago crearMetodoPago() { return new PagoQR(codigoQR); }
}

/*
PATRÓN ESTRUCTURAL: DECORATOR

Propósito: añadir responsabilidades (descuento, recargo por tarjeta, recargo por QR, 
IVA, envío...) al cálculo del total de forma DINÁMICA, sin modificar la clase 
base ni crear una explosión de subclases. Alternativa flexible a la herencia.

Participantes:
    - Component: TotalVenta (interfaz)
    - ConcreteComponent: TotalBase
    - Decorator: TotalDecorator (abstracta)
    - ConcreteDecorator: DescuentoDecorator, RecargoTarjetaDecorator, RecargoQRDecorator
*/

interface TotalVenta {
    int calcular();
    String descripcion();
}

class TotalBase implements TotalVenta {
    private final int subtotal;

    public TotalBase(int subtotal) { this.subtotal = subtotal; }
    @Override 
    public int calcular() { return subtotal; }
    @Override 
    public String descripcion() { return "Subtotal: " + subtotal; }
}

abstract class TotalDecorator implements TotalVenta {
    protected final TotalVenta componente;

    protected TotalDecorator(TotalVenta c) { this.componente = c; }
    @Override 
    public int calcular() { return componente.calcular(); }
    @Override 
    public String descripcion() { return componente.descripcion(); }
}

class DescuentoDecorator extends TotalDecorator {
    private final int porcentaje;

    public DescuentoDecorator(TotalVenta c, int p) { super(c); this.porcentaje = p; }
    @Override 
    public int calcular() {
        return componente.calcular() - (componente.calcular() * porcentaje / 100);
    }
    @Override 
    public String descripcion() {
        return componente.descripcion() + " − Descuento " + porcentaje + "%";
    }
}

class RecargoTarjetaDecorator extends TotalDecorator {
    private static final int RECARGO = 5;

    public RecargoTarjetaDecorator(TotalVenta c) { super(c); }
    @Override 
    public int calcular() {
        return componente.calcular() + (componente.calcular() * RECARGO / 100);
    }
    @Override 
    public String descripcion() {
        return componente.descripcion() + " + Recargo tarjeta " + RECARGO + "%";
    }
}

class RecargoQRDecorator extends TotalDecorator {
    private static final int RECARGO = 2;

    public RecargoQRDecorator(TotalVenta c) { super(c); }
    @Override public int calcular() {
        return componente.calcular() + (componente.calcular() * RECARGO / 100);
    }
    @Override public String descripcion() {
        return componente.descripcion() + " + Recargo QR " + RECARGO + "%";
    }
}

class Venta {
    private final Celular celular;
    private final int cantidad;
    private final int total;
    private final int descuento;
    private final MetodoPago metodoPago;

    public Venta(Celular c, int cantidad, int descuento, int total, MetodoPago mp) {
        this.celular = c; 
        this.cantidad = cantidad;
        this.descuento = descuento; 
        this.total = total; 
        this.metodoPago = mp;
    }

    public void mostrarVenta() {
        System.out.println("Celular: " + celular.getMarca() + " " + celular.getModelo());
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Descuento: " + descuento + "%");
        System.out.println("Método de pago: " + metodoPago.getNombreMetodo());
        System.out.println("Total: " + total);
    }
}

//  Tienda orquesta todo. NO conoce clases concretas de pago ni cómo se calcula el total (solo usa interfaces).
class TiendaCelulares {
    private final List<Celular> inventario = new ArrayList<>();

    public void cargarDatosIniciales() {
        inventario.add(new Celular("Samsung", "Galaxy S21", 800, 10));
        inventario.add(new Celular("Apple",   "iPhone 13", 1000, 5));
        inventario.add(new Celular("Xiaomi",  "Mi 11",      600, 8));
    }

    public void agregarCelular(Celular c) {
        inventario.add(c);
        System.out.println("Celular registrado en el inventario.");
    }

    public void mostrarInventario() {
        if (inventario.isEmpty()) { 
            System.out.println("El inventario está vacío."); 
            return; 
        }
        System.out.println("Inventario de celulares:");
        for (Celular c : inventario) { 
            c.consultarInformacion(); System.out.println(); 
        }
    }

    public Celular buscarModelo(String modelo) {
        for (Celular c : inventario) {
            if (c.getModelo().equalsIgnoreCase(modelo)) return c;
        }
        System.out.println("Celular no encontrado en el inventario.");
        return null;
    }

    public Venta realizarVenta(Celular celular, int cantidad, int descuento, MetodoPago mp) {
        if (celular == null) { 
            System.out.println("No se puede realizar la venta."); 
            return null; 
        }
        if (cantidad > celular.getStock()) {
            System.out.println("No hay suficiente stock para vender " + cantidad + " unidades.");
            return null;
        }

        int subtotal = celular.getPrecio() * cantidad;

        // DECORATOR utilizado para calcular el total con descuento y recargos de forma dinámica
        TotalVenta total = new TotalBase(subtotal);
        if (descuento > 0) total = new DescuentoDecorator(total, descuento);

        // Aplicamos el recargo según la estrategia de pago
        if (mp instanceof PagoTarjeta) total = new RecargoTarjetaDecorator(total);
        else if (mp instanceof PagoQR) total = new RecargoQRDecorator(total);

        System.out.println("\nDetalle del cálculo");
        System.out.println(total.descripcion());
        System.out.println("Total final: " + total.calcular());

        // STRATEGY utilizado para realizar el pago según la estrategia elegida
        mp.pagar();

        Venta venta = new Venta(celular, cantidad, descuento, total.calcular(), mp);
        venta.mostrarVenta();
        celular.actualizarStock(cantidad);
        System.out.println("Stock restante: " + celular.getStock());
        return venta;
    }
}

class Main {
    static Scanner sc = new Scanner(System.in);
    static TiendaCelulares tienda = new TiendaCelulares();

    public static void main(String[] args) {
        tienda.cargarDatosIniciales();
        int opcion;
        do {
            mostrarMenu();
            opcion = sc.nextInt(); sc.nextLine();
            switch (opcion) {
                case 1 -> registrarCelular();
                case 2 -> tienda.mostrarInventario();
                case 3 -> realizarVenta();
                case 4 -> buscarCelular();
                case 5 -> System.out.println("Saliendo del programa...");
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
        } while (opcion != 5);
    }

    static void mostrarMenu() {
        System.out.println("\n1. Agregar celular");
        System.out.println("2. Mostrar inventario");
        System.out.println("3. Realizar venta");
        System.out.println("4. Buscar celular");
        System.out.println("5. Salir");
    }

    static void registrarCelular() {
        System.out.print("Marca: ");  
        String marca = sc.nextLine();
        System.out.print("Modelo: "); 
        String modelo = sc.nextLine();
        System.out.print("Precio: "); 
        int precio = sc.nextInt(); 
        sc.nextLine();
        System.out.print("Stock: ");  
        int stock  = sc.nextInt(); 
        sc.nextLine();
        tienda.agregarCelular(new Celular(marca, modelo, precio, stock));
    }

    static void buscarCelular() {
        System.out.print("Ingrese el modelo del celular a vender: ");
        String modelo = sc.nextLine();
        Celular celular = tienda.buscarModelo(modelo);
        if (celular != null) celular.consultarInformacion();
    }

    static void realizarVenta() {
        System.out.print("Modelo a vender: ");
        Celular celular = tienda.buscarModelo(sc.nextLine());
        if (celular == null) return;

        System.out.print("Cantidad: ");    
        int cantidad  = sc.nextInt(); 
        sc.nextLine();
        System.out.print("Descuento %: "); 
        int descuento = sc.nextInt(); 
        sc.nextLine();

        // El cliente elige que ConcreteCreator usar
        System.out.println("Seleccione método de pago:");
        System.out.println("1. Efectivo");
        System.out.println("2. Tarjeta (+5% recargo)");
        System.out.println("3. QR (+2% recargo)");
        System.out.print("Opción: ");
        int mpOpcion = sc.nextInt(); sc.nextLine();

        // El cliente instancia el ConcreteCreator concreto (esto es lo normal en Factory Method puro)
        MetodoPagoFactory factory;
        switch (mpOpcion) {
            case 1 -> factory = new PagoEfectivoFactory();
            case 2 -> factory = new PagoTarjetaFactory();
            case 3 -> {
                System.out.print("Ingrese código QR: ");
                String codigo = sc.nextLine();
                factory = new PagoQRFactory(codigo);
            }
            default -> { 
                System.out.println("Método inválido."); 
                return; 
            }
        }

        // El cliente usa SOLO la interfaz MetodoPago (no conoce PagoEfectivo/PagoTarjeta/PagoQR)
        MetodoPago metodoPago = factory.crearMetodoPago();
        tienda.realizarVenta(celular, cantidad, descuento, metodoPago);
    }
}
