package cn.ldbz.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class LdbzWishlist  implements Serializable {

	private static final long serialVersionUID = 5162189266133983206L;

	private Long id;
    
    private Date created;

    private Long creator;
    
    private String creatorName;
    
    private Long itemCode ;
    
    private Long userId ;
    
    private Integer num ;
    
    private Integer status ;
    
    private String image ;
    
    private String title ;
    
    private Float price ;
    
}
