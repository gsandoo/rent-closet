package com.closet.rent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.closet.san.BoardBiz;
import com.closet.san.BoardDAO;
import com.closet.san.BoardDTO;
import com.closet.san.Criteria;
import com.closet.san.ItemsDTO;
import com.closet.san.ItemsService;

@Controller
public class sanController {
	private static final Logger logger = LoggerFactory.getLogger(sanController.class);
	
	@Autowired
	private BoardDAO dao;
	
	@Autowired
	private BoardBiz biz;
	
	
	@Autowired
	private ItemsService itemsService;
	
	@RequestMapping("/list.do")
	public String serviceBoard(Model model) {
		logger.info("SERVICE PAGE");
		model.addAttribute("list",biz.selectList());
		
		return "san/serviceBoard"; // serviceBoard.jsp 로 이동 시켜준다
	}
	
	// 게시글 누를 시 글 상세 보기
	@RequestMapping("/one.do")
	public String one(Model model, int bdNum,HttpServletResponse response, HttpServletRequest request) throws IOException {
		logger.info("SELECT ONE");
		logger.info("UPDATE visit");

		PrintWriter out = response.getWriter();
		
		
		
		HttpSession session = request.getSession();
		String name = (String) session.getAttribute("mem_name");
		
		// 조회수 증가
		BoardDTO dto  = biz.selectOne(bdNum);
		int num = dto.getBdNum();
		int count = biz.updateVisit(num, dto);
		System.out.println(count);
		
		
		System.out.println("세션 값으로 받아온 이름은:" + name);
		
		if(name!=null) {
			String writer = dto.getWriter();
			System.out.println("게시글 작성자의 이름은:" + writer);
			
			// 세션값이랑 이름이 같은지 비교
			if(name.equals(writer)) {
			    session.setAttribute("writer", dto.getWriter()); // 세션에 값 넣기
			    return "san/selectOne";
			}else {
					return "san/selectOne";
				}
		}else {
			model.addAttribute("dto", dto);
			return "san/selectOne";
		}
		
		
		
	}
	
	@RequestMapping("/insert.do")
	public String insert(Model model,HttpServletRequest request,HttpServletResponse response) throws IOException {
		logger.info("INSERT ONE");
		HttpSession session = request.getSession();
		String writer = (String) session.getAttribute("mem_name");
		if(writer != null) {
			model.addAttribute("writer", writer ); // 모델에 값을 넣어 보냅니다. 이건 view 에서 활용 가능 
			return "san/insert";
		}else {
			response.setContentType("text/html; charset=UTF=8");

			System.out.println("writer는 지금"+ writer+ "입니다");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('please login!');</script>");
			out.flush();
			return "eun/login";
		}
		
		
	}
	
	@RequestMapping("/insertBoard.do")
	public void insertBoard(BoardDTO dto,HttpServletResponse response, HttpServletRequest request)throws Exception {
		logger.info("INSERT BOARD");
		PrintWriter out = response.getWriter();
		int res = biz.insert(dto);
		
		if(res > 0) {
			out.println("<script>");
			out.println("alert('article has been written!')");
		    out.println("location.href='/'"); 
		    out.println("</script>");
		}else {
			
			out.println("<script>");
			out.println("alert('article has not been written!')");
		    out.println("location.href='/'"); 
		    out.println("</script>"); 
		}
		
	}

	// 게시글 수정
	@RequestMapping("/update.do")
	public String update(Model model, int bdNum) {
		
		logger.info("UPDATE PAGE");
		
		BoardDTO dto = biz.selectOne(bdNum);
		model.addAttribute("dto", dto);
		
		return "san/update";
		
	}
	
	@RequestMapping("/updateBoard.do")
	public void updateBoard(BoardDTO dto,HttpServletResponse response, HttpServletRequest request)throws Exception{
		
		logger.info("UPDATE");
		
		PrintWriter out = response.getWriter();
		int res = biz.update(dto);
		
		if(res > 0) {
			out.println("<script>");
			out.println("alert('article has been modified!')");
		    out.println("location.href='/'"); 
		    out.println("</script>");
		}else {
			
			out.println("<script>");
			out.println("alert('article has not been modified!')");
		    out.println("location.href='/'"); 
		    out.println("</script>"); 
		}
		
						
	}
	
	@RequestMapping("/delete.do")
	public void delete(int bdNum,HttpServletResponse response) throws IOException {
		logger.info("DELETE");
		
		PrintWriter out = response.getWriter();

		int res = biz.delete(bdNum);

		if(res > 0) {
			out.println("<script>");
			out.println("alert('article has been deleted!')");
		    out.println("location.href='/'"); 
		    out.println("</script>");
		}else {
			
			out.println("<script>");
			out.println("alert('article has not been deleted!')");
		    out.println("location.href='/'"); 
		    out.println("</script>"); 
		}
		
	};

	
	// 상품등록
	@RequestMapping("/Enroll.do")
	public String Enroll(Model model , HttpServletRequest request) {
		logger.info("상품등록 페이지");
		HttpSession session = request.getSession();
		String writer = (String) session.getAttribute("mem_name");
		model.addAttribute("writer", writer);
		return "san/goodsEnroll";
		
		
	}
	
	@RequestMapping("/goodsEnroll.do")
	public void goodsEnrollPOST(ItemsDTO dto,  HttpServletResponse response, HttpServletRequest request )throws Exception{
		logger.info("클로젯 셰어 누른 후");
		
		int res = itemsService.itemsEnroll(dto);
	
		PrintWriter out = response.getWriter();
		
		if(res > 0) {
			out.println("<script>");
			out.println("alert('items enrolled!')");
		    out.println("location.href='/'"); 
		    out.println("</script>");
		}else {
			
			out.println("<script>");
			out.println("alert('items have not been enrolled!')");
		    out.println("location.href='/'"); 
		    out.println("</script>"); 
		}		
	}
	
	/* 첨부 파일 업로드 */
	@RequestMapping("/uploadAjaxAction.do")
	public void uploadAjaxActionPOST(MultipartFile[] uploadFile) { // 매개변수를 LIST 로 받을 시 에러 나고 있음
		
		logger.info("클로젯 셰어 사진등록 중 ...");
		
		// 향상된 for
				for(MultipartFile multipartFile : uploadFile) {
					logger.info("-----------------------------------------------");
					logger.info("파일 이름 : " + multipartFile.getOriginalFilename());
					logger.info("파일 타입 : " + multipartFile.getContentType());
					logger.info("파일 크기 : " + multipartFile.getSize());			
				}
				
				//기본 for
				for(int i = 0; i < uploadFile.length; i++) {
					logger.info("-----------------------------------------------");
					logger.info("파일 이름 : " + uploadFile[i].getOriginalFilename());
					logger.info("파일 타입 : " + uploadFile[i].getContentType());
					logger.info("파일 크기 : " + uploadFile[i].getSize());			
				}
	}
	
	
	
	// 상의 이동

	@RequestMapping("cate.do")
	public String cate(Model model,@RequestParam("cate_code") int cate_code) {
		logger.info("카테고리 페이지");
		
		List<ItemsDTO> dto =  itemsService.selectItemsList(cate_code);
		model.addAttribute("list", dto);

		return "san/tops";
	}
	
	
	
	
	@RequestMapping("requestupload.do")
    public String requestupload2(MultipartHttpServletRequest mtfRequest) {
        List<MultipartFile> fileList = mtfRequest.getFiles("file");
        String src = mtfRequest.getParameter("src");
        System.out.println("src value : " + src);

        String path = "C:\\image\\";

        for (MultipartFile mf : fileList) {
            String originFileName = mf.getOriginalFilename(); // 원본 파일 명
            long fileSize = mf.getSize(); // 파일 사이즈

            System.out.println("originFileName : " + originFileName);
            System.out.println("fileSize : " + fileSize);

            String safeFile = path + System.currentTimeMillis() + originFileName;
            try {
                mf.transferTo(new File(safeFile));
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return "eun/main";
    }
//	@RequestMapping("more.do")
//	public String itemsListGET(Model model, Criteria cri) {
//		logger.info("상품을 보여 드립니다.");
//		
//		List<ItemsDTO> criteria = itemsService.getItemsPaging(cri);
//		criteria.forEach(items->logger.info(""+items));
//		
//		model.addAttribute("list", criteria );
//		
//		return "san/moreItems";
//	}
	
	
	

}





