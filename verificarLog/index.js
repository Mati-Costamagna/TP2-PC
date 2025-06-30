const fs = require('fs');
const path = require('path');

// Ruta al archivo de log de estadísticas
const rutaArchivo = path.join(__dirname, '..',  'log_estadisticas.txt');

try {
    // Leer el archivo de forma síncrona
    const contenido = fs.readFileSync(rutaArchivo, 'utf8');
    
    const out = contenido.split("Politica implementada:")[0];
    console.log(out);
    
    // Expresiones regulares corregidas
    let rg_simple = /\b5\s+6\b/g;
    let rg_media = /\b2\s+3\s+4\b/g;
    let rg_compleja = /\b7\s+8\s+9\s+10\b/g;
    
    // Contar matches para cada regex
    const matches_simple = (out.match(rg_simple) || []).length;
    const matches_media = (out.match(rg_media) || []).length;
    const matches_compleja = (out.match(rg_compleja) || []).length;
    
    // Conteo de cada número del 0 al 11
    const conteoNumeros = {};
    
    // Inicializar contadores
    for (let i = 0; i <= 11; i++) {
        conteoNumeros[i] = 0;
    }
    
    // Buscar cada número en el contenido
    for (let i = 0; i <= 11; i++) {
        const regex = new RegExp(`\\b${i}\\b`, 'g');
        const matches = out.match(regex) || [];
        conteoNumeros[i] = matches.length;
    }
    
    // Crear carpeta validaciones si no existe
    const carpetaValidaciones = path.join(__dirname, 'validaciones');
    if (!fs.existsSync(carpetaValidaciones)) {
        fs.mkdirSync(carpetaValidaciones);
    }
    
    // Generar timestamp
    const ahora = new Date();
    const timestamp = ahora.getFullYear() + '-' + 
                     String(ahora.getMonth() + 1).padStart(2, '0') + '-' + 
                     String(ahora.getDate()).padStart(2, '0') + '-' + 
                     String(ahora.getHours()).padStart(2, '0') + '-' + 
                     String(ahora.getMinutes()).padStart(2, '0') + '-' + 
                     String(ahora.getSeconds()).padStart(2, '0');
    
    // Nombre del archivo con timestamp
    const nombreArchivo = `log_estadisticas_${timestamp}.txt`;
    const rutaDestino = path.join(carpetaValidaciones, nombreArchivo);
    
    // Copiar el archivo original
    fs.copyFileSync(rutaArchivo, rutaDestino);
    
    // Preparar contenido adicional para agregar al final
    let contenidoAdicional = '\n\n=== RESULTADOS DE VALIDACIONES ===\n';
    contenidoAdicional += `Fecha de validación: ${ahora.toLocaleString()}\n\n`;
    
    // Agregar resultados de matches
    contenidoAdicional += '=== RESULTADOS DE MATCHES ===\n';
    contenidoAdicional += `Regex Simple (5 6): ${matches_simple} matches\n`;
    contenidoAdicional += `Regex Media (2 3 4): ${matches_media} matches\n`;
    contenidoAdicional += `Regex Compleja (7 8 9 10): ${matches_compleja} matches\n\n`;
    
    // Agregar conteo de números
    contenidoAdicional += '=== CONTEO DE NÚMEROS (0-11) ===\n';
    for (let i = 0; i <= 11; i++) {
        contenidoAdicional += `Número ${i}: ${conteoNumeros[i]} apariciones\n`;
    }
    contenidoAdicional += '\n';
    
    // Agregar validaciones
    contenidoAdicional += '=== VALIDACIONES ===\n';
    
    // Validación 1: 0, 1 y 11 deben respetar la jerarquía 0 > 1 > 11
    const val0 = conteoNumeros[0];
    const val1 = conteoNumeros[1];
    const val11 = conteoNumeros[11];
    
    if (val0 >= val1 && val1 >= val11) {
        if (val0 === val1 && val1 === val11) {
            contenidoAdicional += '✅ Entrada y salida total bien\n';
        } else {
            contenidoAdicional += '⚠️  WARNING: Los números 0, 1 y 11 no son iguales pero respetan la jerarquía\n';
            contenidoAdicional += `   - Número 0: ${val0}\n`;
            contenidoAdicional += `   - Número 1: ${val1}\n`;
            contenidoAdicional += `   - Número 11: ${val11}\n`;
        }
    } else {
        contenidoAdicional += '❌ ERROR: No se respeta la jerarquía 0 > 1 > 11\n';
        contenidoAdicional += `   - Número 0: ${val0}\n`;
        contenidoAdicional += `   - Número 1: ${val1}\n`;
        contenidoAdicional += `   - Número 11: ${val11}\n`;
    }
    
    // Validación 2: Complejidad simple - 5 y 6 deben respetar la jerarquía 5 > 6
    if (conteoNumeros[5] >= conteoNumeros[6]) {
        if (conteoNumeros[5] === conteoNumeros[6]) {
            contenidoAdicional += '✅ Complejidad simple OK\n';
        } else {
            contenidoAdicional += '⚠️  WARNING: Los números 5, 6 no son iguales pero respetan la jerarquía\n';
            contenidoAdicional += `   - Número 5: ${conteoNumeros[5]}\n`;
            contenidoAdicional += `   - Número 6: ${conteoNumeros[6]}\n`;
        }
    } else {
        contenidoAdicional += '❌ ERROR: No se respeta la jerarquía 5 > 6\n';
        contenidoAdicional += `   - Número 5: ${conteoNumeros[5]}\n`;
        contenidoAdicional += `   - Número 6: ${conteoNumeros[6]}\n`;
    }

    // Validación 3: Complejidad media - 2, 3, 4 deben respetar la jerarquía 2 > 3 > 4
    if (conteoNumeros[2] >= conteoNumeros[3] && conteoNumeros[3] >= conteoNumeros[4]) {
        if (conteoNumeros[2] === conteoNumeros[3] && conteoNumeros[3] === conteoNumeros[4]) {
            contenidoAdicional += '✅ Complejidad media OK\n';
        } else {
            contenidoAdicional += '⚠️  WARNING: Los números 2, 3, 4 no son iguales pero respetan la jerarquía\n';
            contenidoAdicional += `   - Número 2: ${conteoNumeros[2]}\n`;
            contenidoAdicional += `   - Número 3: ${conteoNumeros[3]}\n`;
            contenidoAdicional += `   - Número 4: ${conteoNumeros[4]}\n`;
        }
    } else {
        contenidoAdicional += '❌ ERROR: No se respeta la jerarquía 2 > 3 > 4\n';
        contenidoAdicional += `   - Número 2: ${conteoNumeros[2]}\n`;
        contenidoAdicional += `   - Número 3: ${conteoNumeros[3]}\n`;
        contenidoAdicional += `   - Número 4: ${conteoNumeros[4]}\n`;
    }
    
    // Validación 4: Complejidad alta - 7, 8, 9, 10 deben respetar la jerarquía 7 > 8 > 9 > 10
    if (conteoNumeros[7] >= conteoNumeros[8] && conteoNumeros[8] >= conteoNumeros[9] && conteoNumeros[9] >= conteoNumeros[10]) {
        if (conteoNumeros[7] === conteoNumeros[8] && conteoNumeros[8] === conteoNumeros[9] && conteoNumeros[9] === conteoNumeros[10]) {
            contenidoAdicional += '✅ Complejidad alta OK\n';
        } else {
            contenidoAdicional += '⚠️  WARNING: Los números 7, 8, 9, 10 no son iguales pero respetan la jerarquía\n';
            contenidoAdicional += `   - Número 7: ${conteoNumeros[7]}\n`;
            contenidoAdicional += `   - Número 8: ${conteoNumeros[8]}\n`;
            contenidoAdicional += `   - Número 9: ${conteoNumeros[9]}\n`;
            contenidoAdicional += `   - Número 10: ${conteoNumeros[10]}\n`;
        }
    } else {
        contenidoAdicional += '❌ ERROR: No se respeta la jerarquía 7 > 8 > 9 > 10\n';
        contenidoAdicional += `   - Número 7: ${conteoNumeros[7]}\n`;
        contenidoAdicional += `   - Número 8: ${conteoNumeros[8]}\n`;
        contenidoAdicional += `   - Número 9: ${conteoNumeros[9]}\n`;
        contenidoAdicional += `   - Número 10: ${conteoNumeros[10]}\n`;
    }
    
    // Validación 4: Suma de caminos (2 + 5 + 7) debe ser igual a 0, pero error solo si no respeta 1 > (2 + 5 + 7)
    const sumaCaminos = conteoNumeros[2] + conteoNumeros[5] + conteoNumeros[7];
    
    if (val1 >= sumaCaminos) {
        if (val0 === sumaCaminos) {
            contenidoAdicional += '✅ Suma de caminos correcta\n';
        } else {
            contenidoAdicional += '⚠️  WARNING: La suma de caminos (2 + 5 + 7) no es igual al número 0 pero respeta la jerarquía\n';
            contenidoAdicional += `   - Suma (2 + 5 + 7): ${sumaCaminos}\n`;
            contenidoAdicional += `   - Número 0: ${val0}\n`;
            contenidoAdicional += `   - Número 1: ${val1}\n`;
        }
    } else {
        contenidoAdicional += '❌ ERROR: No se respeta la jerarquía 1 > (2 + 5 + 7)\n';
        contenidoAdicional += `   - Suma (2 + 5 + 7): ${sumaCaminos}\n`;
        contenidoAdicional += `   - Número 0: ${val0}\n`;
        contenidoAdicional += `   - Número 1: ${val1}\n`;
    }
    
    // Agregar información del archivo
    const lineas = contenido.split('\n');
    contenidoAdicional += `\nEl archivo original tiene ${lineas.length} líneas\n`;
    contenidoAdicional += `Archivo de validación: ${nombreArchivo}\n`;
    
    // Agregar el contenido adicional al archivo copiado
    fs.appendFileSync(rutaDestino, contenidoAdicional);
    
    // Mostrar en consola también
    console.log('\n=== RESULTADOS DE MATCHES ===');
    console.log(`Regex Simple (5 6): ${matches_simple} matches`);
    console.log(`Regex Media (2 3 4): ${matches_media} matches`);
    console.log(`Regex Compleja (7 8 9 10): ${matches_compleja} matches`);
    
    console.log('\n=== CONTEO DE NÚMEROS (0-11) ===');
    for (let i = 0; i <= 11; i++) {
        console.log(`Número ${i}: ${conteoNumeros[i]} apariciones`);
    }
    
    console.log('\n=== VALIDACIONES ===');
    console.log(contenidoAdicional.split('=== VALIDACIONES ===')[1]);
    
    console.log(`\n✅ Archivo guardado en: ${rutaDestino}`);
    
} catch (error) {
    console.error('Error al procesar el archivo:', error.message);
}
