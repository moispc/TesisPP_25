package com.example.food_front;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.food_front.models.MercadoPagoPreference;
import com.example.food_front.utils.DiagnosticUtil;
import com.example.food_front.utils.MercadoPagoService;
import com.example.food_front.utils.NetworkUtils;
import com.example.food_front.utils.SessionManager;
import com.example.food_front.utils.ConnectionMonitor;

public class PaymentFragment extends Fragment implements ConnectionMonitor.ConnectivityListener {

    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioButtonMercadoPago, radioButtonCreditCard;
    private Button buttonPayNow, buttonRetry;
    private ProgressBar progressBar;
    private WebView webViewPayment;
    private View paymentLayout, webViewLayout, errorLayout;
    private TextView textViewError;
    private static final String TAG = "PaymentFragment";
    private MercadoPagoService mercadoPagoService;
    private SessionManager sessionManager;
    private String currentPaymentRequestId;
    
    // Constantes para los errores de WebView
    private static final int ERROR_TIMEOUT = -8;
    private static final int ERROR_HOST_LOOKUP = -2;
    private static final int ERROR_CONNECT = -6;
    private static final int ERROR_IO = -7;

    // Timeout en milisegundos para la carga de una página
    private static final int PAGE_LOAD_TIMEOUT = 20000; // 20 segundos
    private Runnable timeoutRunnable;
    private Handler timeoutHandler = new Handler();
    
    // Monitor de conexión
    private ConnectionMonitor connectionMonitor;
    private boolean isNetworkAvailable = true;

    public PaymentFragment() {
        // Constructor vacío requerido
    }
      @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Inicializar servicios
        mercadoPagoService = new MercadoPagoService(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        // Inicializar el monitor de conexión
        connectionMonitor = new ConnectionMonitor(requireContext(), this);

        // Inicializar vistas
        radioButtonCreditCard = view.findViewById(R.id.radioButton4);
        radioButtonMercadoPago = view.findViewById(R.id.radioButton6);
        RadioButton radioButtonPaypal = view.findViewById(R.id.radioButtonPaypal);
        buttonPayNow = view.findViewById(R.id.button2);
        buttonRetry = view.findViewById(R.id.buttonRetry);
        progressBar = view.findViewById(R.id.progressBarPayment);
        webViewPayment = view.findViewById(R.id.webViewPayment);
        paymentLayout = view.findViewById(R.id.paymentLayout);
        webViewLayout = view.findViewById(R.id.webViewLayout);
        errorLayout = view.findViewById(R.id.errorLayout);
        textViewError = view.findViewById(R.id.textViewError);        
        // Configurar texto para la opción de MercadoPago
        radioButtonMercadoPago.setText("MercadoPago");
        // Configurar texto para la opción de PayPal
        radioButtonPaypal.setText("PayPal");
        
        // Configurar WebView según las recomendaciones de MercadoPago
        WebSettings webSettings = webViewPayment.getSettings();
        webSettings.setJavaScriptEnabled(true);  // MercadoPago requiere JavaScript
        webSettings.setDomStorageEnabled(true);  // Permitir almacenamiento DOM
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);  // No usar caché
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);  // Permitir popups
        webSettings.setSupportMultipleWindows(true);  // Soportar múltiples ventanas
        webSettings.setLoadWithOverviewMode(true);  // Cargar la página ajustándola a la pantalla
        webSettings.setUseWideViewPort(true);  // Usar viewport ancho        webSettings.setBuiltInZoomControls(false);  // Desactivar controles de zoom
        webSettings.setSupportZoom(false);  // Desactivar zoom
        
        webViewPayment.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "URL cargada: " + url);
                
                // Verificar si la URL es una de las URLs de retorno
                // De acuerdo con la documentación de MercadoPago, debemos verificar las URLs de retorno
                if (url.contains("/exito") || url.contains("/success") || 
                    url.contains("ispcfood.netlify.app/exito") || 
                    url.contains("success=true") || 
                    url.contains("status=approved")) {
                    
                    Log.d(TAG, "Detectada URL de pago exitoso: " + url);
                    handlePaymentSuccess();
                    return true;
                } else if (url.contains("/error") || url.contains("/failure") || 
                          url.contains("ispcfood.netlify.app/error") || 
                          url.contains("status=rejected")) {
                    
                    Log.d(TAG, "Detectada URL de pago rechazado: " + url);
                    handlePaymentFailure();
                    return true;
                } else if (url.contains("/pendiente") || url.contains("/pending") || 
                          url.contains("ispcfood.netlify.app/pendiente") || 
                          url.contains("status=pending") || url.contains("status=in_process")) {
                    
                    Log.d(TAG, "Detectada URL de pago pendiente: " + url);
                    handlePaymentPending();
                    return true;
                }
                
                // Si no es una URL de retorno, permitir la carga normalmente
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showWebViewProgress(true);
                
                // Establecer un timeout para la carga de la página
                startPageLoadingTimeout(view, url);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showWebViewProgress(false);
                
                // Cancelar el temporizador ya que la página se ha cargado
                cancelPageLoadingTimeout();
                
                // Log para depuración
                Log.d(TAG, "Página cargada correctamente: " + url);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView error: " + description + " (código: " + errorCode + ") URL: " + failingUrl);
                
                // Verificar si el error es en la URL principal de pago o en un recurso secundario
                if (failingUrl != null && failingUrl.contains("mercadopago")) {
                    // Si es un error de tiempo de espera o de conectividad, mostrar un mensaje específico
                    if (errorCode == ERROR_TIMEOUT || errorCode == ERROR_HOST_LOOKUP || 
                        errorCode == ERROR_CONNECT || errorCode == ERROR_IO) {
                        
                        // Error de conectividad
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWebViewProgress(false);
                                showErrorView(true, "Error de conexión con MercadoPago. Por favor verifica tu conexión a Internet e intenta nuevamente.");
                                
                                // Agregar un botón para recargar directamente
                                if (buttonRetry != null) {
                                    buttonRetry.setText("Reintentar conexión");
                                }
                            }
                        });
                    } else {
                        // Otros errores
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWebViewProgress(false);
                                showErrorView(true, "Error al cargar la página de pago de MercadoPago: " + description);
                            }
                        });
                    }
                } else {
                    // Si es un recurso secundario, solo log pero no interrumpir
                    Log.w(TAG, "Error en recurso secundario, continuando carga: " + failingUrl);
                }
            }
        });// Configurar el botón de pago
        buttonPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButtonMercadoPago.isChecked()) {
                    procesarPagoConMercadoPago();
                } else if (radioButtonCreditCard.isChecked()) {
                    Toast.makeText(requireContext(), "Pago con tarjeta no implementado aún", Toast.LENGTH_SHORT).show();
                } else if (radioButtonPaypal.isChecked()) {
                    // Simular pago exitoso con PayPal: ir directo a SuccessFragment
                    requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, new SuccessFragment())
                        .addToBackStack(null)
                        .commit();
                } else {
                    Toast.makeText(requireContext(), "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Configurar el botón de reintento
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showErrorView(false, null);
                procesarPagoConMercadoPago();
            }
        });        return view;
    }
    
    private void procesarPagoConMercadoPago() {
        showLoading(true);
        showErrorView(false, null);
        
        // Verificar conectividad a Internet
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showErrorView(true, "No hay conexión a Internet. Por favor, verifica tu conexión e intenta nuevamente.");
            return;
        }
        
        // Intentar verificar rápidamente conexión al servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Timeout más corto para mejorar la experiencia de usuario
                final boolean serverReachable = NetworkUtils.checkUrlAvailability("https://backmp.onrender.com", 3000);
                
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!serverReachable) {
                            Log.w(TAG, "Servidor aparentemente no alcanzable, pero intentaremos igualmente");
                            // Aunque el servidor no parezca alcanzable, intentamos de todas formas
                            // ya que la verificación no siempre es precisa
                            Toast.makeText(requireContext(), "Conectando con el servidor de pagos...", Toast.LENGTH_SHORT).show();
                        }
                        
                        // Continuar con el proceso en cualquier caso
                        continuarProcesoPago();
                    }
                });
            }        }).start();
    }
    
    private void continuarProcesoPago() {
        // Obtener email del usuario desde SessionManager (si está disponible)
        String userEmail = sessionManager.getUserEmail();

        // Si no hay email disponible, mostrar error y no continuar
        if (userEmail == null || userEmail.isEmpty() || userEmail.equals("usuario@example.com")) {
            showLoading(false);
            showErrorView(true, "Debes tener un email válido en tu perfil para realizar el pago. Inicia sesión nuevamente o completa tu perfil.");
            Toast.makeText(requireContext(), "No se puede continuar sin email de usuario", Toast.LENGTH_LONG).show();
            return;
        }
        // Mostrar mensaje de log para depuración
        Log.d(TAG, "Iniciando proceso de pago con MercadoPago. Email: " + userEmail);
        // Log extra: mostrar el token y el email que se enviarán
        Log.d(TAG, "Token enviado: " + sessionManager.getToken());
        // Log extra: mostrar el body que se enviará (en MercadoPagoService)
        mercadoPagoService.createPreference(userEmail, new MercadoPagoService.MercadoPagoCallback() {
            @Override
            public void onSuccess(MercadoPagoPreference preference) {
                Log.d(TAG, "Preferencia creada exitosamente: " + preference.getPreferenceId());
                Log.d(TAG, "URL de pago: " + preference.getInitPoint());
                
                if (preference.getInitPoint() == null || preference.getInitPoint().isEmpty()) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoading(false);
                            showErrorView(true, "No se pudo obtener la URL de pago. Por favor, intenta nuevamente.");
                        }
                    });
                    return;
                }
                
                // Guardar el ID de la solicitud de pago
                currentPaymentRequestId = preference.getPaymentRequestId();
                // Guardar la URL de pago en SessionManager
                sessionManager.saveLastInitPoint(preference.getInitPoint());
                // Mostrar el WebView y cargar la URL de pago en la app
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "Redirigiendo a MercadoPago...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(preference.getInitPoint()));
                        startActivity(intent);
                        showWebView(false);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error al crear preferencia: " + errorMessage);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        // Mejorar feedback para errores de carrito vacío
                        if (errorMessage != null && (errorMessage.toLowerCase().contains("carrito") || errorMessage.toLowerCase().contains("empty") || errorMessage.toLowerCase().contains("vacío"))) {
                            showErrorView(true, "Tu carrito está vacío. Agrega productos antes de intentar pagar.");
                        } else if (errorMessage != null && (errorMessage.contains("500") || errorMessage.toLowerCase().contains("servidor"))) {
                            // Error 500 u otro error de servidor: ofrecer abrir en navegador
                            new AlertDialog.Builder(requireContext())
                                .setTitle("Error en el servidor de pagos")
                                .setMessage("Ocurrió un error al crear la preferencia de pago. ¿Quieres intentar abrir el pago en el navegador web?")
                                .setPositiveButton("Abrir en navegador", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Intentar abrir la URL de pago si está disponible
                                        String lastInitPoint = (currentPaymentRequestId != null) ? sessionManager.getLastInitPoint() : null;
                                        if (lastInitPoint != null && !lastInitPoint.isEmpty()) {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(lastInitPoint));
                                            startActivity(browserIntent);
                                        } else {
                                            Toast.makeText(requireContext(), "No se pudo obtener la URL de pago.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                            showErrorView(true, "Error al crear el pago: " + errorMessage);
                        } else {
                            showErrorView(true, "Error al crear el pago: " + errorMessage);
                        }
                    }
                });
            }        });
    }
    
    private void handlePaymentSuccess() {
        showWebView(false);
        Log.d(TAG, "Pago exitoso! Redirigiendo a pantalla de confirmación");

        // Confirmar el pedido en el backend usando el paymentRequestId
        if (currentPaymentRequestId != null && !currentPaymentRequestId.isEmpty()) {
            showLoading(true);
            mercadoPagoService.confirmOrder(currentPaymentRequestId, new MercadoPagoService.OrderConfirmCallback() {
                @Override
                public void onSuccess(String orderId) {
                    showLoading(false);
                    // Mostrar un mensaje más informativo al usuario
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Pago Exitoso");
                    builder.setMessage("Tu pago ha sido procesado correctamente. ¡Gracias por tu compra!");
                    builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Navegar al fragmento de éxito
                            navigateToSuccessFragment();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    // Mostrar error de confirmación
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Error al confirmar pedido");
                    builder.setMessage("El pago fue exitoso, pero hubo un error al confirmar el pedido: " + errorMessage + "\nPor favor, contacta soporte si no recibes tu pedido.");
                    builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setCancelable(true);
                    builder.show();
                }
            });
        } else {
            // Si no hay paymentRequestId, mostrar éxito pero advertir
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Pago Exitoso");
            builder.setMessage("Tu pago ha sido procesado, pero no se pudo confirmar el pedido automáticamente. Si no recibes tu pedido, contacta soporte.");
            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    navigateToSuccessFragment();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }
    
    private void handlePaymentFailure() {
        showWebView(false);
        
        // Mostrar un diálogo con información clara sobre el fallo
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pago no completado");
        builder.setMessage("Lo sentimos, el pago no pudo ser procesado. Por favor, intenta con otro método de pago o contacta a tu entidad bancaria.");
        builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Reintentar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                procesarPagoConMercadoPago();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    
    private void handlePaymentPending() {
        showWebView(false);
        
        // Mostrar un diálogo con información clara sobre el estado pendiente
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pago en Proceso");
        builder.setMessage("Tu pago está siendo procesado. Puede tomar unos minutos. Recibirás una notificación cuando se complete.");
        builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Podríamos redirigir a una pantalla de estado pendiente aquí
            }
        });
        builder.setCancelable(true);        builder.show();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonPayNow.setEnabled(!show);
    }
    
    private void showWebViewProgress(boolean show) {
        View progressBar = getView().findViewById(R.id.progressBarWebView);
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showWebView(boolean show) {
        webViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        paymentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        showLoading(false);
    }
    
    private void showErrorView(boolean show, String errorMessage) {
        if (show && errorMessage != null) {
            textViewError.setText(errorMessage);
        }
          errorLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        webViewLayout.setVisibility(show ? View.GONE : webViewLayout.getVisibility());
        paymentLayout.setVisibility(show ? View.GONE : paymentLayout.getVisibility());
    }
    
    private void verifyPaymentStatus(String paymentRequestId) {
        Log.d(TAG, "URL de retorno de MercadoPago detectada");
        
        // Como no estamos utilizando el endpoint de verificación de estado del backend,
        // simplemente confiamos en la URL de retorno proporcionada por MercadoPago
          // Si llegamos aquí desde una URL de éxito, asumimos que el pago se completó correctamente
        handlePaymentSuccess();
    }
    
    private void navigateToSuccessFragment() {
        SuccessFragment successFragment = new SuccessFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Añadir animación a la transición
        fragmentTransaction.setCustomAnimations(
            R.anim.slide_in_right, 
            R.anim.slide_out_left
        );
        fragmentTransaction.replace(R.id.fragment_container_view, successFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    
    /**
     * Inicia un temporizador para la carga de la página.
     * Si la página no termina de cargar en el tiempo establecido, muestra un error.
     */
    private void startPageLoadingTimeout(final WebView webView, final String url) {
        // Cancelar cualquier temporizador existente
        cancelPageLoadingTimeout();
        
        // Crear un nuevo temporizador
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // Si aún estamos mostrando el progreso, es que la página no ha terminado de cargar
                if (webViewLayout.getVisibility() == View.VISIBLE && 
                    getView() != null && 
                    getView().findViewById(R.id.progressBarWebView).getVisibility() == View.VISIBLE) {
                    
                    Log.e(TAG, "Timeout al cargar la página: " + url);
                    
                    // Intentar detener la carga
                    webView.stopLoading();
                    
                    // Mostrar un error al usuario
                    showErrorView(true, "La página de pago está tardando demasiado en cargar. " +
                                      "Por favor, verifica tu conexión a Internet e intenta nuevamente.");
                }
            }
        };
        
        // Programar el temporizador
        timeoutHandler.postDelayed(timeoutRunnable, PAGE_LOAD_TIMEOUT);
    }
    
    /**
     * Cancela cualquier temporizador de carga de página en curso
     */
    private void cancelPageLoadingTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }    @Override
    public void onStart() {
        super.onStart();
        
        // Iniciar el monitoreo de conexión
        if (connectionMonitor != null) {
            connectionMonitor.startMonitoring();
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        // Limpiar recursos y cancelar temporizadores
        cancelPageLoadingTimeout();
        
        // Detener cualquier carga en progreso en el WebView
        if (webViewPayment != null) {
            webViewPayment.stopLoading();
        }
        
        // Detener el monitoreo de conexión
        if (connectionMonitor != null) {
            connectionMonitor.stopMonitoring();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Limpiar recursos del WebView
        if (webViewPayment != null) {
            webViewPayment.destroy();
        }
    }
  

    @Override
    public void onConnected() {
        isNetworkAvailable = true;
        
        // Solo actuamos si estamos en la vista de error y el error era de conexión
        if (errorLayout != null && errorLayout.getVisibility() == View.VISIBLE &&
            textViewError.getText().toString().contains("conexión")) {
            
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireContext(), "Conexión restablecida", Toast.LENGTH_SHORT).show();
                    
                    // Si estábamos intentando cargar una página, reintentamos
                    if (currentPaymentRequestId != null) {
                        showErrorView(false, null);
                        procesarPagoConMercadoPago();
                    }
                }
            });
        }
    }    @Override
    public void onDisconnected() {
        isNetworkAvailable = false;
        
        // Si estamos cargando el WebView, mostramos el error
        if (webViewLayout != null && webViewLayout.getVisibility() == View.VISIBLE) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWebViewProgress(false);
                    showErrorView(true, "Se ha perdido la conexión a Internet. Por favor, verifica tu conexión y reintenta.");
                }
            });
        }
    }
}
