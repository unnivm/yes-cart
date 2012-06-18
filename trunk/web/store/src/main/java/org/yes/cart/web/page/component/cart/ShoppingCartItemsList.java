package org.yes.cart.web.page.component.cart;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.yes.cart.constants.ServiceSpringKeys;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.ProductSku;
import org.yes.cart.domain.entity.SkuPrice;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.service.domain.CategoryService;
import org.yes.cart.service.domain.ImageService;
import org.yes.cart.service.domain.ProductService;
import org.yes.cart.service.domain.ProductSkuService;
import org.yes.cart.shoppingcart.CartItem;
import org.yes.cart.shoppingcart.impl.AddSkuToCartEventCommandImpl;
import org.yes.cart.shoppingcart.impl.RemoveAllSkuFromCartCommandImpl;
import org.yes.cart.shoppingcart.impl.RemoveSkuFromCartCommandImpl;
import org.yes.cart.shoppingcart.impl.SetSkuQuantityToCartEventCommandImpl;
import org.yes.cart.web.page.CheckoutPage;
import org.yes.cart.web.page.HomePage;
import org.yes.cart.web.page.ShoppingCartPage;
import org.yes.cart.web.page.component.BaseComponent;
import org.yes.cart.web.page.component.price.PriceView;
import org.yes.cart.web.support.constants.StorefrontServiceSpringKeys;
import org.yes.cart.web.support.constants.WebParametersKeys;
import org.yes.cart.web.support.entity.decorator.ProductSkuDecorator;
import org.yes.cart.web.support.entity.decorator.impl.ProductSkuDecoratorImpl;
import org.yes.cart.web.support.service.AttributableImageService;
import org.yes.cart.web.util.WicketUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 10/9/11
 * Time: 11:30 AM
 */
public class ShoppingCartItemsList extends ListView<CartItem> {


    // ------------------------------------- MARKUP IDs BEGIN ---------------------------------- //


    private static final String PRICE_PANEL = "pricePanel";
    private static final String SKU_NUM_LABEL = "skunum";
    private static final String ADD_ONE_LINK = "addOneLink";
    private static final String REMOVE_ONE_LINK = "removeOneLink";
    private static final String REMOVE_ALL_LINK = "removeAllLink";
    private static final String PRODUCT_LINK = "productLink";
    private static final String PRODUCT_NAME_LABEL = "name";
    private static final String DEFAULT_IMAGE = "defaultImage";
    private static final String QUANTITY_TEXT = "quantity";


    private static final String LINE_TOTAL_VIEW = "lineAmountView";
    private final static String PRICE_VIEW = "skuPriceView";

    private static final String QUANTITY_ADJUST_BUTTON = "quantityButton";

    // ------------------------------------- MARKUP IDs END ---------------------------------- //

    @SpringBean(name = ServiceSpringKeys.PRODUCT_SKU_SERVICE)
    private ProductSkuService productSkuService;

    @SpringBean(name = StorefrontServiceSpringKeys.ATTRIBUTABLE_IMAGE_SERVICE)
    protected AttributableImageService attributableImageService;


    @SpringBean(name = ServiceSpringKeys.CATEGORY_SERVICE)
    protected CategoryService categoryService;

    @SpringBean(name = ServiceSpringKeys.IMAGE_SERVICE)
    protected ImageService imageService;


    private final Category rootCategory;


    /**
     * Construct list of product in shopping cart.
     *
     * @param id        component id
     * @param cartItems cart items
     */
    public ShoppingCartItemsList(final String id, final List<? extends CartItem> cartItems) {
        super(id, cartItems);
        rootCategory = categoryService.getRootCategory();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateItem(final ListItem<CartItem> cartItemListItem) {

        final CartItem cartItem = cartItemListItem.getModelObject();

        final String skuCode = cartItem.getProductSkuCode();

        final ProductSku sku = productSkuService.getProductSkuBySkuCode(skuCode);

        final ProductSkuDecorator productSkuDecorator = new ProductSkuDecoratorImpl(
                imageService,
                attributableImageService,
                categoryService,
                sku,
                WicketUtil.getHttpServletRequest().getContextPath()
        );


        cartItemListItem.add(
                createAddOneSkuLink(skuCode)
        ).add(
                createRemoveAllSkuLink(skuCode)
        ).add(
                createRemoveOneSkuLink(skuCode)
        ).add(
                new Label(SKU_NUM_LABEL, skuCode)
        ).add(
                getProductLink(sku)
        ).add(
                new PriceView(PRICE_VIEW, new Pair<BigDecimal, BigDecimal>(cartItem.getPrice(), null), null, false)
        ).add(
                new PriceView(LINE_TOTAL_VIEW, new Pair<BigDecimal, BigDecimal>(
                        cartItem.getPrice().multiply(cartItem.getQty()), null), null, false)
        );


        final TextField<Integer> quantity = new TextField<Integer>(QUANTITY_TEXT,
                new Model<Integer>(cartItem.getQty().intValue()));

        cartItemListItem.add(
                quantity
        ).add(
                createAddSeveralSkuButton(skuCode, quantity)
        );


        final String [] size = productSkuDecorator.getThumbnailImageSize(rootCategory);

        final String width = size[0];
        final String height = size[1];

        final String defaultImageAttributeName = productSkuDecorator.getDefaultImageAttributeName();   //TODO new attribute plus optimaze
        final String defaultImageRelativePath = productSkuDecorator.getImage(width, height, defaultImageAttributeName);

        cartItemListItem.add(new ContextImage(DEFAULT_IMAGE, defaultImageRelativePath)
                .add(
                        new AttributeModifier(BaseComponent.HTML_WIDTH, width),
                        new AttributeModifier(BaseComponent.HTML_HEIGHT, height)
                )
        );


    }


    /**
     * Adjust quantity.
     *
     * @param productSkuCode sku code
     * @param qtyField       quantity input box
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private Button createAddSeveralSkuButton(final String productSkuCode, final TextField<Integer> qtyField) {
        final Button adjustQuantityButton = new Button(QUANTITY_ADJUST_BUTTON) {
            @Override
            public void onSubmit() {
                String qty = qtyField.getInput();
                String skuCode;
                try {
                    skuCode = URLEncoder.encode(productSkuCode, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    skuCode = productSkuCode;
                }


                if (NumberUtils.isDigits(qty) && NumberUtils.toInt(qty) >= 1) {
                    qtyField.setConvertedInput(new Integer(qty));
                    setResponsePage(
                            ShoppingCartPage.class,
                            new PageParameters()
                                    .add(SetSkuQuantityToCartEventCommandImpl.CMD_KEY, skuCode)
                                    .add(SetSkuQuantityToCartEventCommandImpl.CMD_PARAM_QTY, qty)
                    );


                } else {
                    qtyField.setConvertedInput(BigDecimal.ONE.intValue());
                    error(getLocalizer().getString("nonzerodigits", this, "Need positive integer value"));
                }
            }
        };
        adjustQuantityButton.setDefaultFormProcessing(true);
        return adjustQuantityButton;
    }

    /**
     * Get link to show product with selected in cart product sku.
     *
     * @param productSku product    sku
     * @return link to show product and selected SKU
     */
    private Link getProductLink(final ProductSku productSku) {
        final Link productLink = new BookmarkablePageLink<HomePage>(
                PRODUCT_LINK, HomePage.class,
                new PageParameters().set(WebParametersKeys.SKU_ID, String.valueOf(productSku.getId()))
        );
        productLink.add(new Label(PRODUCT_NAME_LABEL, productSku.getName()).setEscapeModelStrings(false));
        return productLink;
    }


    /**
     * Create BookmarkablePageLink for add one sku to cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for add one sku to cart command
     */
    private BookmarkablePageLink createAddOneSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(AddSkuToCartEventCommandImpl.CMD_KEY, skuCode);
        return new BookmarkablePageLink<ShoppingCartPage>(
                ADD_ONE_LINK,
                ShoppingCartPage.class,
                paramsMap
        );
    }

    /**
     * Create BookmarkablePageLink for remove one sku from cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private BookmarkablePageLink createRemoveOneSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(RemoveSkuFromCartCommandImpl.CMD_KEY, skuCode);
        return new BookmarkablePageLink<ShoppingCartPage>(
                REMOVE_ONE_LINK,
                ShoppingCartPage.class,
                paramsMap
        );
    }


    /**
     * Create BookmarkablePageLink for remove one sku from cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private BookmarkablePageLink createRemoveAllSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(RemoveAllSkuFromCartCommandImpl.CMD_KEY, skuCode);
        return new BookmarkablePageLink<ShoppingCartPage>(
                REMOVE_ALL_LINK,
                ShoppingCartPage.class,
                paramsMap
        );
    }


}
