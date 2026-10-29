# Sistema-de-Ventas-de-Celulares
# Sistema de Gestión de Ventas de Celulares con Patrones de Diseño

## Descripción

Módulo de venta de celulares que permite **registrar**, **consultar**, **buscar** y **vender** celulares aplicando descuentos y recargos según el método de pago, actualizando el stock automáticamente.

El proyecto demuestra la implementación de **tres patrones de diseño** (uno de cada grupo):
- **Factory Method** (Creacional)
- **Decorator** (Estructural)
- **Strategy** (Comportamiento)

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| **Java** | JDK 17+ | Lenguaje de programación |
| **POO** | — | Paradigma base (clases, interfaces, herencia, polimorfismo) |
| **IDE** | VS Code | Compilación y ejecución |

> No se usan frameworks externos. La persistencia es en memoria (`List<Celular>`).

---

## Caso de uso

### Contexto
Una tienda de celulares necesita un módulo para:
1. **Registrar** nuevos celulares (marca, modelo, precio, stock).
2. **Consultar** el inventario completo.
3. **Buscar** un celular por modelo.
4. **Realizar ventas** aplicando:
   - Descuento configurable por el vendedor.
   - Recargo automático según método de pago (Efectivo 0%, Tarjeta +5%, QR +2%).
   - Actualización del stock tras la venta.

### Flujo principal (realizar venta)
1. El vendedor ingresa el modelo del celular.
2. El sistema lo busca en el inventario; si no existe, cancela la venta.
3. El vendedor ingresa cantidad, descuento y método de pago (Efectivo / Tarjeta / QR).
4. Si elige QR, ingresa el código.
5. El sistema:
   - Valida stock suficiente.
   - Calcula subtotal (`precio × cantidad`).
   - Aplica descuento y recargo según método de pago.
   - Ejecuta el pago.
   - Muestra el detalle y actualiza el stock.

### Reglas de negocio
- No se puede vender más que el stock disponible.
- Descuento en porcentaje entero (0–100).
- Recargo según método de pago: Efectivo 0%, Tarjeta +5%, QR +2%.
- Total final = `(subtotal − descuento) + recargo`.

---

## Patrones de diseño implementados

| # | Patrón | Grupo | Propósito en el sistema | Clases involucradas |
|---|--------|-------|--------------------------|---------------------|
| 1 | **Factory Method** | Creacional | Encapsular la creación de objetos `MetodoPago` en fábricas concretas, desacoplando `Main` de las clases concretas de pago. | `MetodoPagoFactory` (Creator), `PagoEfectivoFactory`, `PagoTarjetaFactory`, `PagoQRFactory` (ConcreteCreators), `MetodoPago` (Product), `PagoEfectivo`, `PagoTarjeta`, `PagoQR` (ConcreteProducts) |
| 2 | **Decorator** | Estructural | Añadir dinámicamente responsabilidades al cálculo del total (descuento, recargo por tarjeta, recargo por QR) sin modificar la clase base ni crear explosión de subclases. | `TotalVenta` (Component), `TotalBase` (ConcreteComponent), `TotalDecorator` (Decorator), `DescuentoDecorator`, `RecargoTarjetaDecorator`, `RecargoQRDecorator` (ConcreteDecorators) |
| 3 | **Strategy** | Comportamiento | Definir una familia de algoritmos de pago intercambiables, permitiendo que el cliente use `MetodoPago` sin conocer la implementación concreta. | `MetodoPago` (Strategy), `PagoEfectivo`, `PagoTarjeta`, `PagoQR` (ConcreteStrategies), `TiendaCelulares` (Context) |


- **Factory Method** decide **qué estrategia concreta** se crea.
- **Strategy** define **cómo se paga**.
- **Decorator** define **cómo se calcula el total** según la estrategia elegida (recargo por tarjeta/QR).

---

## Factory Method (Creacional) — Razón de uso

**¿Por qué se decidió usar este patrón?**

Al diseñar el sistema se identificó que la creación de los métodos de pago era un punto crítico: el cliente (menú principal) necesitaba obtener objetos `MetodoPago` (Efectivo, Tarjeta, QR), pero **no debía depender de sus clases concretas**. Si el cliente instanciara directamente `new PagoEfectivo()`, `new PagoTarjeta()` o `new PagoQR(...)`, quedaría acoplado a los productos y cualquier nuevo método de pago obligaría a modificar el cliente, violando el principio **Open/Closed**.

Se eligió **Factory Method** porque:
- **Desacopla al cliente de los productos concretos**: el cliente solo conoce la interfaz `MetodoPago` y las fábricas; nunca las clases concretas de pago.
- **Centraliza la lógica de creación**: cada fábrica concreta (`PagoEfectivoFactory`, `PagoTarjetaFactory`, `PagoQRFactory`) sabe cómo construir su producto, incluyendo parámetros específicos como el código QR.
- **Facilita la extensión**: para añadir un nuevo método de pago (ej. `PagoCripto`) basta con crear su fábrica y registrarla en el cliente; la tienda y la interfaz `MetodoPago` no se modifican.

---

## Decorator (Estructural) — Razón de uso

**¿Por qué se decidió usar este patrón?**

El cálculo del total de una venta es inherentemente **variable y combinatorio**: puede incluir descuento, recargo por tarjeta, recargo por QR y, a futuro, IVA, envío o cupones. Si se resolviera con **herencia**, se necesitaría una subclase por cada combinación posible (`TotalConDescuentoYRecargoTarjeta`, `TotalConDescuentoYRecargoQR`, `TotalSinDescuentoConRecargoTarjeta`, etc.), produciendo una **explosión combinatoria de clases** (2ⁿ subclases para n decoraciones). Además, el total debe poder calcularse **en tiempo de ejecución** según las elecciones del vendedor.

Se eligió **Decorator** porque:
- **Añade responsabilidades dinámicamente**: el total se construye apilando decoradores (`new DescuentoDecorator(new RecargoQRDecorator(new TotalBase(subtotal)))`), combinándolos según la venta concreta.
- **Evita la explosión de subclases**: cada responsabilidad (descuento, recargo tarjeta, recargo QR) es una clase independiente que se compone por delegación.
- **Respeta Open/Closed**: para añadir un nuevo recargo o impuesto, solo se crea un nuevo decorador; `TotalBase` y `TotalDecorator` no se tocan.
- **Mantiene una interfaz única** (`TotalVenta` con `calcular()` y `descripcion()`): la tienda sigue trabajando con la abstracción sin saber cuántos decoradores hay apilados.

---

## Strategy (Comportamiento) — Razón de uso

**¿Por qué se decidió usar este patrón?**

Los métodos de pago son algoritmos intercambiables que comparten una misma responsabilidad: ejecutar el cobro y declarar su política de recargo. Si la tienda resolviera el pago con un `if/else` o `switch` interno, tendría la responsabilidad de conocer todos los métodos de pago, violando **SRP** (la tienda no debería saber cómo paga cada método) y **Open/Closed** (cada nuevo método obligaría a modificar la tienda).

Se eligió **Strategy** porque:
- **Define una familia de algoritmos encapsulados** (`pagar()`, `getNombreMetodo()`, `getRecargoPorcentaje()`), cada uno en su propia clase (`PagoEfectivo`, `PagoTarjeta`, `PagoQR`).
- **Los hace intercambiables**: la tienda recibe un `MetodoPago` y llama `pagar()` sin saber si es efectivo, tarjeta o QR; el **polimorfismo** resuelve la elección.
- **Elimina condicionales** en el contexto: no hay `if (tipo == "tarjeta")` dentro de `TiendaCelulares`; cada estrategia concreta implementa su propio comportamiento y recargo.
- **Permite variar el algoritmo independientemente del cliente**: añadir `PagoCripto` no requiere tocar `TiendaCelulares`, solo crear una nueva estrategia y su fábrica.

---

## Síntesis de la elección de patrones

| Necesidad del sistema | Patrón elegido | Razón de la elección |
|-----------------------|----------------|----------------------|
| Crear métodos de pago sin acoplar al cliente a clases concretas | **Factory Method** | Jerarquía de productos + creadores desacoplados; extensible sin modificar el cliente |
| Calcular el total combinando descuentos y recargos sin explosión de subclases | **Decorator** | Responsabilidades combinables en tiempo de ejecución; evita herencia múltiple y combinatoria |
| Ejecutar distintos algoritmos de pago de forma intercambiable | **Strategy** | Familia de algoritmos con interfaz común; elimina condicionales y respeta SRP/OCP |

> Cada patrón responde a una **necesidad concreta y distinta**: **crear** (Factory Method), **componer** (Decorator) y **ejecutar** (Strategy).

---
## Cómo ejecutar

### Requisitos
- JDK 17 o superior instalado (`java -version`).

### Compilar y ejecutar
```bash
# Compilar
javac Main.java

# Ejecutar
java Main
