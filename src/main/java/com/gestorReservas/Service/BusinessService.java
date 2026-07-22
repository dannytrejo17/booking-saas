package com.gestorReservas.Service;

import com.gestorReservas.Dto.BusinessDto;
import com.gestorReservas.Dto.ScheduleDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.BusinessSchedule;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.BusinessScheduleRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BusinessScheduleRepository businessScheduleRepository;
    private final CloudinaryService cloudinaryService;


    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository,
                           ServiceRepository serviceRepository,
                           BusinessScheduleRepository businessScheduleRepository,
                           CloudinaryService cloudinaryService) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.businessScheduleRepository = businessScheduleRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public String createBusiness(Principal principal, String name, String slug,
                                 String email, String phone, String address, String logo) {

        User owner = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

        validateCreateBusiness(owner, name, slug);

        Business business = new Business();
        business.setUser(owner);
        business.setName(name.trim());
        business.setSlug(slug.trim().toLowerCase());
        business.setEmail(isBlank(email) ? null : email.trim());
        business.setPhone(phone);
        business.setAddress(address);
        business.setLogo(logo);
        businessRepository.save(business);

        owner.setBusiness(business);
        userRepository.save(owner);

        return "negocio creado";
    }

    private void validateCreateBusiness(User owner, String name, String slug) {
        if (isBlank(name) || isBlank(slug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name y slug son obligatorios");
        }

        if (owner.getBusiness() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ya tienes un negocio creado");
        }

        String normalizedSlug = slug.trim().toLowerCase();
        if (businessRepository.existsBySlug(normalizedSlug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el slug ya esta en uso");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String editBusiness(String name, String slug, String email,
                               String phone, String address, String logo, Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if(business == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        String normalizedSlug = slug.trim().toLowerCase();
        String currentSlug = business.getSlug() == null ? "" : business.getSlug().trim().toLowerCase();
        if (!normalizedSlug.equals(currentSlug) && businessRepository.existsBySlug(normalizedSlug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el slug ya esta en uso");
        }

        business.setName(name);
        business.setSlug(normalizedSlug);
        business.setEmail(email);
        business.setPhone(phone);
        business.setAddress(address);
        business.setLogo(logo);
        businessRepository.save(business);

        return "negocio editado";
    }

    public String deleteBusiness(Principal principal){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if(business == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        List<com.gestorReservas.Model.Service> service = serviceRepository.findByBusiness_BusinessId(business.getBusinessId());

        if(!service.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "elimina los servicios primero");
        }

        user.setBusiness(null);
        userRepository.save(user);
        businessRepository.delete(business);

        return "negocio eliminado";
    }


    public BusinessDto getBusinessBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));
        return BusinessDto.from(business);
    }

    public Map<String, String> uploadImage(MultipartFile file, String type, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        if (type == null || (!type.equals("cover") && !type.equals("logo"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "type debe ser cover o logo");
        }

        String url = cloudinaryService.uploadImage(file, business.getBusinessId(), type);
        if (type.equals("cover")) {
            business.setCoverImage(url);
        } else {
            business.setLogo(url);
        }
        businessRepository.save(business);

        return Map.of(
                "message", "imagen subida",
                "type", type,
                "url", url
        );
    }



    public String createSchedule(Principal principal,
    LocalTime openTime,
    LocalTime closeTime,
    DayOfWeek dayOfWeek
    ){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "horario invalido");
        }


        if (dayOfWeek == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dayOfWeek es obligatorio");
        }

        boolean exists = businessScheduleRepository.existsByBusinessAndDayOfWeekAndOpenTimeAndCloseTime(
                business, dayOfWeek, openTime, closeTime);
        if (exists) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ese horario ya existe");
        }

        BusinessSchedule businessSchedule = new BusinessSchedule();

        businessSchedule.setBusiness(business);
        businessSchedule.setOpenTime(openTime);
        businessSchedule.setCloseTime(closeTime);
        businessSchedule.setDayOfWeek(dayOfWeek);
        businessScheduleRepository.save(businessSchedule);

        return "horario creado";
    }


    public List<ScheduleDto> getSchedule(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        List<BusinessSchedule> schedules = businessScheduleRepository.findByBusiness_BusinessId(business.getBusinessId());

        List<ScheduleDto> result = new ArrayList<>();
        for (BusinessSchedule schedule : schedules) {
            result.add(ScheduleDto.from(schedule));
        }

        return result;


    }

    public String deleteSchedule(DayOfWeek dayOfWeek , Principal principal){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        if(dayOfWeek == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el dia es obligatorio");
        }

        List<BusinessSchedule> schedules = businessScheduleRepository.findByDayOfWeekAndBusiness_BusinessId(dayOfWeek, business.getBusinessId());
        if(schedules.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no hay horario ese dia");
        }else{
            businessScheduleRepository.deleteAll(schedules);
        }

        return "horario eliminado";
    }

}


