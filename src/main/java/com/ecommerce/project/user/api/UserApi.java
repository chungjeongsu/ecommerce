package com.ecommerce.project.user.api;

import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;

public interface UserApi {

    AuthorizeSellerRes authorizeSeller(AuthorizeSellerReq request);
}
