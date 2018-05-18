package com.luxmart.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.luxmart.model.CartItem;
import com.luxmart.model.Product;
import com.luxmart.repository.CartItemRepository;
import com.luxmart.repository.ProductRepository;
import com.luxmart.repository.ShoppingCartRepository;
import com.luxmart.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;

@Controller
@RequestMapping("/stripe")
public class StripeController {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	ShoppingCartRepository shoppingCartRepository;

	@Autowired
	UserRepository userRepository;

	@RequestMapping("/")
	public String stripe(Model model) {
		List<Product> productList = (List<Product>) productRepository.findAll();

		List<CartItem> cartItemList = (List<CartItem>) cartItemRepository.findAll();

		BigDecimal total = new BigDecimal(0);

		for (CartItem item : cartItemList) {
			total = total.add(item.getSubtotal());

		}

		model.addAttribute("productList", productList);
		model.addAttribute("cartItemList", cartItemList);
		// get the absolute value
		model.addAttribute("total", total.abs());
		

		return "stripe-cart";
	}

	// add an item to the cart
	@RequestMapping("/addToCart")
	public String addToCart(@RequestParam Long id) {

		// find the product by id
		Product product = productRepository.findOne(id);

		CartItem cartItem = new CartItem();
		cartItem.setProduct(product);
		cartItem.setQty(1);
		// limit decimal points
		cartItem.setSubtotal(new BigDecimal(product.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));

		// save to database
		cartItemRepository.save(cartItem);
		return "redirect:/stripe/";
	}

	// delete an item from the cart
	@RequestMapping("/remove")
	public String remove(@RequestParam Long id) {

		cartItemRepository.delete(id);
		return "redirect:/stripe/";

	}

	// update the cart
	@RequestMapping(value = "/updateCart", method=RequestMethod.POST)
	public String updateCart(HttpServletRequest request){
		
		Long id = Long.parseLong(request.getParameter("id"));
		int qty = Integer.parseInt(request.getParameter("qty"));
		CartItem cartItem = cartItemRepository.findOne(id);
		cartItem.setQty(qty);
		cartItem.setSubtotal(new BigDecimal(cartItem.getProduct().getPrice()*qty).setScale(2, BigDecimal.ROUND_HALF_UP));
		
		// save the changes
		cartItemRepository.save(cartItem);
		return "redirect:/stripe/";
		}
	
	// Process the checkout stripe payment -- one time payment
	@RequestMapping(value = "/checkoutPay", method=RequestMethod.POST)
	public String checkoutPay(HttpServletRequest request, Model model) 
			throws AuthenticationException, InvalidRequestException, APIConnectionException, 
			CardException, APIException{
		
		// Set your secret key: remember to change this to your live secret key in production
		// See your keys here: https://dashboard.stripe.com/account/apikeys
		Stripe.apiKey = "sk_test_T3b8cyknjJPEbQMrvRUTPnWa";

		// Token is created using Checkout or Elements!
		// Get the payment token ID submitted by the form:
		String token = request.getParameter("stripeToken");

		// Charge the user's card:
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("amount", 1000);
		params.put("currency", "cad");
		params.put("description", "Example charge");
		params.put("source", token);

		
		Charge charge = Charge.create(params);
		
		model.addAttribute("checkoutPaySuccess", true);
		return "forward:/stripe/";
	}
	
	// save card for later charge
	@RequestMapping(value = "/elementsPay", method=RequestMethod.POST)
	public String elementsPay(HttpServletRequest request, Model model) throws AuthenticationException, InvalidRequestException, APIConnectionException, 
	CardException, APIException {
		
		// Set your secret key: remember to change this to your live secret key in production
		// See your keys here: https://dashboard.stripe.com/account/apikeys
		Stripe.apiKey = "sk_test_T3b8cyknjJPEbQMrvRUTPnWa";

		// Token is created using Checkout or Elements!
		// Get the payment token ID submitted by the form:
		String token = request.getParameter("stripeToken");

		// Create a Customer:
		Map<String, Object> customerParams = new HashMap<String, Object>();
		customerParams.put("email", "marcygarcia@outlook.com");
		customerParams.put("source", token);
		Customer customer = Customer.create(customerParams);

		// Charge the Customer instead of the card:
		Map<String, Object> chargeParams = new HashMap<String, Object>();
		chargeParams.put("amount", 1000);
		chargeParams.put("currency", "cad");
		chargeParams.put("customer", customer.getId());
		Charge charge = Charge.create(chargeParams);

		// YOUR CODE: Save the customer ID and other info in a database for later.

		// YOUR CODE (LATER): When it's time to charge the customer again, retrieve the customer ID.
//		Map<String, Object> chargeParams = new HashMap<String, Object>();
//		chargeParams.put("amount", 1500); // $15.00 this time
//		chargeParams.put("currency", "cad");
//		chargeParams.put("customer", customerId);
//		Charge charge = Charge.create(chargeParams);
		
		model.addAttribute("elementsPaySuccess", true);
		return "forward:/stripe/";
	}
	
	
	
}
